import java.net.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.*;

public class broker extends Thread {
	private ServerSocket serverSocket;
	private static String ivKey="0";
	private static String ecom_ip;
	private static int ecom_port;
	private static String eComName;
	private static crypto crypt = new crypto();
	private static String sa=null,sb=null;
	
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
		String user = msg1.substring(0,msg1.length()-24);
		DatabaseConnectivity dbconn = new DatabaseConnectivity();
		Connection conn = dbconn.connectToDatabase();
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

	private static void passMsg(Socket from_skt,Socket to_skt) {
		send_msg(to_skt,get_msg(from_skt));
	}
	
	public void run() {
		while(true) {
			try {
				DatabaseConnectivity dbconn = new DatabaseConnectivity();
				Connection conn = dbconn.connectToDatabase();
				Statement stmt = conn.createStatement();
				
				Socket server = serverSocket.accept();
				Socket client = new Socket(ecom_ip, ecom_port);
				genSessKeyUser(server);

				ResultSet rs = stmt.executeQuery("select * from user_details_paypal where user_name = '"+ eComName+"';");
				if(rs.next()){
					String secKey = rs.getString("user_secret_key");
					getSessKeyEcom(client,secKey);
				} else {
					System.out.println("\n The broker secret key was not found \n");
				}
				System.out.println("Session Key for\n1.User = "+sa+"\n2.Ecom = "+sb);
				getSessKeyClientEcomm(server,client);
//				passMsg(client,server);
				passMsg(client,server);
				server.close();
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
		ecom_ip = args[1];
		ecom_port = Integer.parseInt(args[2]);
		try {
			Thread t = new broker(broker_port);
			t.start();
		} catch(Exception e1) {
			e1.printStackTrace();
		}
	}
}