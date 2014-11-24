import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class user {
	private static String uname, eComName;
	private static String ivKey="0";
	private static crypto crypt = new crypto();
	private static String sa =null, sc=null;

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

	private static String getSessKeyBroker(Socket client,String secKey) throws IllegalArgumentException {
		int n=crypt.randInt(1,1000);
		send_msg(client,uname+crypt.encrypt(secKey,ivKey,Integer.toString(n)));
		String dec_msg1=crypt.decrypt(secKey,ivKey,get_msg(client));
		String sessKey=dec_msg1.substring(0, 16);
		int n_len = Integer.toString(n).length();
		String n1=dec_msg1.substring(16,16+n_len);
		if(Integer.parseInt(n1)!=n) {
			throw new IllegalArgumentException("Aborting connection since Nonces don't match:Expected"+n1+"Received"+dec_msg1.substring(16,16+n_len));
		} else {
			String n2=dec_msg1.substring(16+n_len, dec_msg1.length());
			send_msg(client,crypt.encrypt(sessKey, ivKey, n2+eComName));
		}	   
		sa = sessKey;
		return sessKey;
	}
	
	private static String getSessKeyEcomm(Socket server) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, UnsupportedEncodingException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		String sessionkeyKc = crypt.genKey();
		String msg1 = new String(crypt.RSAEncrypt("amazon", sessionkeyKc));
		send_msg(server,crypt.encrypt(sa, ivKey, msg1));
		String sessKey = crypt.decrypt(sa, ivKey, get_msg(server));
		if(sessKey.equals("got it paypal")){
			System.out.println("Session Key establishment successful \n the session key is = "+sessionkeyKc);
			sc = sessionkeyKc;
		}
		else {
			System.out.println("Session Key Estb Failed\n");
		}
		return sessionkeyKc;
	}

	public static void main(String [] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException  {
		uname = args[0];
		String broker_ip = args[1];
		int broker_port = Integer.parseInt(args[2]);
		eComName = args[3];
		DatabaseConnectivity dbconn = new DatabaseConnectivity();
		Connection conn = dbconn.connectToDatabase();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select * from user_info where broker = 'paypal' AND uname = '"+uname+"';");
		if(rs.next()){
			String secKey = rs.getString("secKey");
			try {
				Socket client = new Socket(broker_ip, broker_port);
				String sessKey=getSessKeyBroker(client,secKey);
				System.out.println("Session Key="+sessKey);
				client.close();
			} catch(IOException e1) {
				System.out.println("Connection Timed out!!!");
				e1.printStackTrace();
			} catch(IllegalArgumentException e2) {
				System.out.println(e2.getMessage());
			}
		} else {
			System.out.println("\n The broker secret key was not found \n");
		}
	}
}