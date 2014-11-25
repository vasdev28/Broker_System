import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.*;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ecommerce extends Thread {
	private ServerSocket serverSocket;
	private static String ivKey="0";
	private static crypto crypt = new crypto();
	private static String sb=null,sc=null;
	
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

	private static void send_list(Socket outsock,ArrayList<String> msg) {
		try {
			ObjectOutputStream objectOutput = new ObjectOutputStream(outsock.getOutputStream());
	        objectOutput.writeObject(msg); 			
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
	}
	
	private static void getSessKeyUser(Socket client) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		String msg1 = crypt.decrypt(sb, ivKey, get_msg(client));
		sc = crypt.RSADecrypt("amazon", msg1);
		send_msg(client, crypt.encrypt(sb, ivKey, crypt.encrypt(sc, ivKey, "got it paypal")));
	}
	
	private static void sendInventory(Socket client) {
		ArrayList<String> item = new ArrayList<String>();
		DatabaseConnectivity db1 = new DatabaseConnectivity();
		crypto crypt1 = new crypto();
		try {
			Connection conn = db1.connectToDatabase();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select * from broker_details_amazon;");
			while(rs.next()) {
		        item.add(rs.getString(1));
		        item.add(rs.getString(2));
		        item.add(rs.getString(3));
			}
			conn.close();
		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		send_list(client,crypt1.encrypt(sc,ivKey,item));
	}
	
	public void run() {
		while(true) {
			try {
				Socket server = serverSocket.accept();
				genSessKeyBroker(server);
				getSessKeyUser(server);
				System.out.println("Session Key for\n1.broker ="+sb+"\n2.User ="+sc);
				sendInventory(server);
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