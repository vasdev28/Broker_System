import java.net.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;

public class broker extends Thread {
	private ServerSocket serverSocket;
	private static String ivKey="0";
	private static String ecom_ip;
	private static int ecom_port;
	private static String eComName=null;
	private static crypto crypt = new crypto();
	private static String sa=null,sb=null;
	private static String user=null;

	public broker(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(10000);
	}

	private static String get_msg(Socket insock) {
		try {
			DataInputStream in = new DataInputStream(insock.getInputStream());
			String msg = in.readUTF();
			return(msg);
		} catch (IOException e) {
			e.printStackTrace();
			return("Error");
		}
	}

	private static void send_msg(Socket outsock,String msg) {
		try {
			DataOutputStream out = new DataOutputStream(outsock.getOutputStream());
			out.writeUTF(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void genSessKeyUser(Socket server) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		String msg1=get_msg(server);
		user = msg1.substring(0,msg1.length()-24);
		DatabaseConnectivity dbconn = new DatabaseConnectivity();
		Connection conn = dbconn.connectToDatabase("broker");
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select * from user_details_paypal where user_name = '" + user.toLowerCase() + "'");
		sa = crypt.genKey();
		if(rs.next()){
			String secKey = rs.getString("user_secret_key");
			String userNonce = crypt.decrypt(secKey,ivKey,msg1.substring(msg1.length()-24,msg1.length()));
			String myNonce = Integer.toString(crypt.randInt(1, 1000));
			send_msg(server,crypt.encrypt(secKey,ivKey,sa+userNonce+myNonce));
			String msg3=crypt.decrypt(sa, ivKey,get_msg(server));
			if(!msg3.startsWith(myNonce)) {
				System.out.println("Nonces dont match for "+user+".Exp:"+myNonce+"\tRxd:"+msg3.substring(0,myNonce.length()));
			}
			eComName = msg3.substring(myNonce.length(),msg3.length());
		} else{
			System.out.println("\n Could not find user sec key \n");
		}
		rs.close();
		stmt.close();
		conn.close();
	}

	private static void getSessKeyEcom(Socket server,String secKey) throws IllegalArgumentException {
		int n=crypt.randInt(1,1000);
		send_msg(server,"paypal"+crypt.encrypt(secKey,ivKey,Integer.toString(n)));
		String dec_msg1=crypt.decrypt(secKey,ivKey,get_msg(server));
		sb=dec_msg1.substring(0, 16);
		int n_len = Integer.toString(n).length();
		String n1=dec_msg1.substring(16,16+n_len);
		if(Integer.parseInt(n1)!=n) {
			throw new IllegalArgumentException("Nonces don't match:Expected"+n1+"Received"+dec_msg1.substring(16,16+n_len));
		} else {
			String n2=dec_msg1.substring(16+n_len, dec_msg1.length());
			send_msg(server,crypt.encrypt(sb, ivKey, n2));
		}	   
	}

	private static void getSessKeyClientEcomm(Socket client, Socket server) {
		// This method helps User get a key from ecommerce website.
		// client - send msg to client socket.
		// server - send msg to server socket.
		send_msg(client,crypt.encrypt(sa,ivKey,"Go Ahead"));
		String msg1 = crypt.decrypt(sa, ivKey, get_msg(client));
		send_msg(server, crypt.encrypt(sb, ivKey, msg1));
		String msg2 = crypt.decrypt(sb, ivKey, get_msg(server));
		send_msg(client, crypt.encrypt(sa, ivKey, msg2));
	}

	private static void e2eSecureCommn(Socket userSock, Socket ecomSock) {
		passMsg(userSock,ecomSock);
		passMsg(ecomSock,userSock);
		passMsg(userSock,ecomSock);
	}

	private static int processPayment(Socket userSock, Socket ecomSock) {
		int order_no=1;
		try {
			DatabaseConnectivity dbconn = new DatabaseConnectivity();
			Connection conn = dbconn.connectToDatabase("broker");
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select max(order_num)+1 as max from order_summary_paypal");
			if (rs.next()) {
				try {
					order_no = Integer.parseInt(rs.getString("max"));
				} catch (NumberFormatException e) {
					order_no = 1;
				}
			}
			rs.close();
			String msg1 = crypt.decrypt(sb, ivKey, get_msg(ecomSock));
			if(msg1.contains("Error")) {
				
				send_msg(userSock,crypt.encrypt(sa,ivKey,msg1));
				conn.close();
				return 0;
				
			}
			int balanceAmountUser= 0, rxd_bill_amt = 65535;
			ResultSet rs1 = stmt.executeQuery("select * from user_details_paypal where user_name = '" + user +"'");
			if(rs1.next()) {
				balanceAmountUser = Integer.parseInt(rs1.getString("user_credit_available"));
				String msg1_reg[] = msg1.split(",.");
				rxd_bill_amt = Integer.parseInt(msg1_reg[2]);
				if(balanceAmountUser < rxd_bill_amt) {
					System.out.println("Insufficient balance:Avlbl Balance ="+balanceAmountUser+"Rxd_bill="+rxd_bill_amt);
					send_msg(userSock,crypt.encrypt(sa,ivKey,"Error: Credit Limit Reached!!!"));
					send_msg(ecomSock,crypt.encrypt(sb, ivKey,"Error: User doesn't have enough credits!!!"));
					conn.close();
					return 0;
				}
			}
			rs1.close();
			String msg2_sub = msg1.substring(5,29);
			send_msg(userSock,crypt.encrypt(sa, ivKey, order_no+msg2_sub));
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			String dateRxd = dateFormat.format(date);

			String queryinsertordersummary = "insert into order_summary_paypal values (" + order_no + ",'" + user + "','pending','"+dateRxd+"','','','" + eComName + "')";
			stmt.executeUpdate(queryinsertordersummary);

			String msg3 = crypt.decrypt(sa, ivKey, get_msg(userSock));
			String msg3_reg[] = msg3.split(",");
			String info2ecom = msg3_reg[0];
			String amt = msg3_reg[1].substring(6,msg3_reg[1].length());
			String signature_user = msg3_reg[2];
			if (crypt.RSAVerify(user, order_no+",Give $"+amt, signature_user)) {
				System.out.println("User's signature verified");
			} else {
				System.out.println("User signature mismatch...aborting");
			}
			int amount = Integer.parseInt(amt);

			String queryupdateordersummary1 = "update order_summary_paypal SET status_of_pay = 'Vendor Ack Pending', date_paid = '" + dateRxd + "', user_signature = '" + signature_user + "' where order_num = " + order_no; 
			stmt.executeUpdate(queryupdateordersummary1);


				String queryupdateuserdetails1 = "update user_details_paypal SET user_credit_available = user_credit_available - " + amount + " where user_name = '" + user + "'";
				stmt.executeUpdate(queryupdateuserdetails1);

				String queryupdateuserdetails2 = "update user_details_paypal SET user_credit_available = user_credit_available + " + amount + " where user_name = '" + eComName + "'";
				stmt.executeUpdate(queryupdateuserdetails2);

			send_msg(ecomSock,crypt.encrypt(sb,ivKey,order_no+info2ecom+"Paid $"+amt));
			String msg5 = crypt.decrypt(sb, ivKey, get_msg(ecomSock));
			String msg5_reg[] = msg5.split(",");
			String ecomsignature = msg5_reg[1];
			if(crypt.RSAVerify(eComName, order_no+",Rxd $"+amt, ecomsignature)) {
				System.out.println("E-Commerce website's signature verified");
			} else {
				System.out.println("E-Commerce signature mismatch...aborting");
			}
			String queryupdateordersummary2 = "update order_summary_paypal SET status_of_pay = 'Paid', date_paid = '" + dateRxd + "', vendor_signature_ack = '" + ecomsignature + "' where order_num = " + order_no; 
			stmt.executeUpdate(queryupdateordersummary2);
			stmt.close();
			conn.close();
			return 1;
		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
			
			return 0;
		}
	}
	
	private static int getEcomDetails() {
		try {
			DatabaseConnectivity dbconn = new DatabaseConnectivity();
			Connection conn = dbconn.connectToDatabase("broker");
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select * from user_details_paypal where user_name = '"+ eComName+"';");
			if(rs.next()) {
				ecom_ip = rs.getString("hostname");
				ecom_port = Integer.parseInt(rs.getString("port"));
				rs.close();
				stmt.close();
				conn.close();
				return 1;
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (Exception e) {
			System.out.println("Error when getting ip/port of ecomm...");
		}
		return 0;
	}

	private static void passMsg(Socket from_skt,Socket to_skt) {
		send_msg(to_skt,get_msg(from_skt));
	}

	public void run() {
		while(true) {
			try {
				Socket server = serverSocket.accept();
				genSessKeyUser(server);
				if(getEcomDetails()!=0) {
					Socket client = new Socket(ecom_ip, ecom_port);
					DatabaseConnectivity dbconn = new DatabaseConnectivity();
					Connection conn = dbconn.connectToDatabase("broker");
					Statement stmt = conn.createStatement();
					ResultSet rs = stmt.executeQuery("select * from user_details_paypal where user_name = '"+ eComName+"';");
					if(rs.next()){
						String secKey = rs.getString("user_secret_key");
						getSessKeyEcom(client,secKey);
					} else {
						System.out.println("\n The broker secret key was not found \n");
					}
					rs.close();
					stmt.close();
					conn.close();
					System.out.println("Session Key for\n1.User = "+sa+"\n2.Ecom = "+sb);
					getSessKeyClientEcomm(server,client);
					e2eSecureCommn(server,client);
					if(processPayment(server,client)!=0) {
						passMsg(client,server);
					}
					server.close();
				}
			} catch(SocketTimeoutException s) {
				System.out.println("Socket timed out!");
				break;
			} catch(IOException e) {
				System.out.println("Unexpected errror");
				break;
			} catch (InstantiationException|IllegalAccessException|ClassNotFoundException|SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String [] args) {
		int broker_port = Integer.parseInt(args[0]);
//		ecom_ip = args[1];
//		ecom_port = Integer.parseInt(args[2]);
		try {
			Thread t = new broker(broker_port);
			t.start();
		} catch(Exception e1) {
			e1.printStackTrace();
		}
	}
}
