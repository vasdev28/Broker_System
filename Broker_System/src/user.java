import java.net.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.*;

public class user {
	private static String ivKey="0";
	private static crypto crypt = new crypto();

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
		send_msg(client,"Alice"+crypt.encrypt(secKey,ivKey,Integer.toString(n)));
		String dec_msg1=crypt.decrypt(secKey,ivKey,get_msg(client));
		String sessKey=dec_msg1.substring(0, 16);
		int n_len = Integer.toString(n).length();
		String n1=dec_msg1.substring(16,16+n_len);
		if(Integer.parseInt(n1)!=n) {
			throw new IllegalArgumentException("Aborting connection since Nonces don't match:Expected"+n1+"Received"+dec_msg1.substring(16,16+n_len));
		} else {
			String n2=dec_msg1.substring(16+n_len, dec_msg1.length());
			send_msg(client,crypt.encrypt(sessKey, ivKey, n2+"Amazon"));
		}	   
		return sessKey;
	}

	public static void main(String [] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException  {
		DatabaseConnectivity dbconn = new DatabaseConnectivity();
		Connection conn = dbconn.connectToDatabase();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select * from alice where broker = 'paypal'");
		if(rs.next()){
			String secKey = rs.getString("secKey");
			String serverName = args[0];
			int port = Integer.parseInt(args[1]);
			try {
				Socket client = new Socket(serverName, port);
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