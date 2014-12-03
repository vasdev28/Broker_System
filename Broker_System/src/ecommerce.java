import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;

public class ecommerce extends Thread {
	private ServerSocket serverSocket;
	private static String ivKey="0";
	private static crypto crypt = new crypto();
	private static String sb=null,sc=null,brokername = null;

	public ecommerce(int port) throws IOException {
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

	private static void genSessKeyBroker(Socket server) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		String msg1=get_msg(server);
		String brokerName = msg1.substring(0,msg1.length()-24);
		DatabaseConnectivity dbconn = new DatabaseConnectivity();
		Connection conn = dbconn.connectToDatabase();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select * from broker_details_amazon where broker_name = '" + brokerName.toLowerCase() + "'");
		sb = crypt.genKey();
		if(rs.next()) {
			String secKey = rs.getString("shared_key");
			String userNonce = crypt.decrypt(secKey,ivKey,msg1.substring(msg1.length()-24,msg1.length()));
			String myNonce = Integer.toString(crypt.randInt(1, 1000));
			send_msg(server,crypt.encrypt(secKey,ivKey,sb+userNonce+myNonce));
			String msg3=crypt.decrypt(sb, ivKey,get_msg(server));
			if(!msg3.startsWith(myNonce)) {
				System.out.println("Nonces dont match for "+brokerName+".Exp:"+myNonce+"\tRxd:"+msg3.substring(0,myNonce.length()));
			}
		} else{
			System.out.println("\n Could not find user sec key \n");
		}
		brokername = brokerName;
	}

	private static void getSessKeyUser(Socket client) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		String msg1 = crypt.decrypt(sb, ivKey, get_msg(client));
		sc = crypt.RSADecrypt("amazon", msg1);
		send_msg(client, crypt.encrypt(sb, ivKey, crypt.encrypt(sc, ivKey, "got it "+brokername.toLowerCase())));
	}

	private static int sendInventory(Socket client, String FilePath) {
		int output_index=0;
		String msg = get_msg(client);
		System.out.println(msg);
		try {
			File myFile = new File(FilePath);
			byte[] mybytearray = new byte[(int) myFile.length()];
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
			bis.read(mybytearray, 0, mybytearray.length);
			String str = crypt.encrypt(sc, sc, Base64.encodeBase64String(mybytearray));
			bis.close();
			send_msg(client,str);
			String get_text_from_client = crypt.decrypt(sc,sc,get_msg(client));
			BufferedReader reader = new BufferedReader(new FileReader(FilePath));
			String line = reader.readLine();
			while(line!=null){
				String fields[] = line.split("\\s+");
				if(fields[4].equals(get_text_from_client)){
					output_index = Integer.parseInt(fields[0]);
					break;
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return output_index;
	}

	private static void initiatePayment(Socket client,int itemNo) {
		String price;
		int bill_no=1,numberOfItemsSold=1;
		DatabaseConnectivity dbconn = new DatabaseConnectivity();
		try {
			Connection conn = dbconn.connectToDatabase();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select * from vendor_inventory_amazon where item_no = " + itemNo);
			if (rs.next()) {
				price = rs.getString("item_price");
			} else price = "65535";
			rs = stmt.executeQuery("select max(bill_no)+1 as max from bill_details_amazon");
			if (rs.next()) {
				try {
					bill_no = Integer.parseInt(rs.getString("max"));
				} catch (NumberFormatException e) {
					bill_no = 1;
				}
			}
			send_msg(client,crypt.encrypt(sb, ivKey,"Bill," + crypt.encrypt(sc,ivKey,bill_no+",Give $"+price)));
			
			int amount = Integer.parseInt(price) * numberOfItemsSold;
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			String dateRxd = dateFormat.format(date);
			String queryInsertaftermsg1 = "insert into broker_transaction_amazon values(0," + bill_no + ",'" + brokername + "'," + amount + ",'" + dateRxd + "','ongoing','" + dateRxd + "')"; 
			System.out.println("Query after msg 1 = " + queryInsertaftermsg1);
			int checkifamazontableinsert = stmt.executeUpdate(queryInsertaftermsg1);
			/*if(checkifamazontableinsert == 0){
				System.out.println("Insertion into broker_transaction_amazon successfully");
			}
			*/
			
			String msg4 = crypt.decrypt(sb,ivKey,get_msg(client));
			String msg4_reg[] = msg4.split("Paid .");
			String rxd_bill_no = crypt.decrypt(sc,ivKey,msg4_reg[0].substring(msg4_reg[0].length()-24,msg4_reg[0].length()));
			String order_num = msg4_reg[0].substring(0,msg4_reg[0].length()-24);
			String rxd_amt = msg4_reg[1];
			int orderNo = Integer.parseInt(order_num);
			
			String queryUpdateAfterRxdMsg = "update broker_transaction_amazon SET order_num = " + orderNo +", payment_rxd_date = '" + dateRxd + "' where bill_no = " + bill_no ;
			int checkifamazontableupdated = stmt.executeUpdate(queryUpdateAfterRxdMsg); 
			/*if(checkifamazontableupdated == 0){
				System.out.println("updation of broker_transaction_amazon successfully");
			}
			*/
			
			String queryinsertBillDetailsAmazon = "insert into bill_details_amazon values (" + bill_no + "," + numberOfItemsSold + "," + itemNo + ")";
			int checkifinsertintoBillDetailsAmazon = stmt.executeUpdate(queryinsertBillDetailsAmazon);
			/*if(checkifinsertintoBillDetailsAmazon == 0){
				System.out.println("Insertion into Bill Details Amazon is successful");
			}
			*/
			send_msg(client,crypt.encrypt(sb,ivKey,order_num+"Signature"));
			
		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void send_file(Socket outsock,String FilePath) {
		try {
			File myFile = new File(FilePath);
			byte[] mybytearray = new byte[(int) myFile.length()];
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
			bis.read(mybytearray, 0, mybytearray.length);
			String str = crypt.encrypt(sc, sc, Base64.encodeBase64String(mybytearray));
			send_msg(outsock,str);
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while(true) {
			try {
				Socket server = serverSocket.accept();
				genSessKeyBroker(server);
				getSessKeyUser(server);
				System.out.println("Session Key for\n1.broker ="+sb+"\n2.User ="+sc);
				int itemRequested = sendInventory(server,"C:\\Users\\AnukulKumar\\git\\Broker_System\\Broker_System\\input.txt");
				System.out.println(itemRequested);
				initiatePayment(server,itemRequested);
				send_file(server,"C:\\Users\\AnukulKumar\\git\\Broker_System\\Broker_System\\s1.pdf");
				System.out.println("File transfer done");
				server.close();
			} catch(SocketTimeoutException s) {
				System.out.println("Socket timed out!");
				break;
			} catch(InvalidKeyException | NoSuchAlgorithmException
					| InvalidKeySpecException | NoSuchPaddingException
					| IllegalBlockSizeException | BadPaddingException|IOException e) {
				System.out.println("Unexpected errror");
				break;
			} catch (InstantiationException|IllegalAccessException|ClassNotFoundException|SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String [] args) {
		int port = Integer.parseInt(args[0]);
		try {
			Thread t = new ecommerce(port);
			t.start();
		} catch(Exception e1) {
			e1.printStackTrace();
		}
	}
}