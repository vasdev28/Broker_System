import java.net.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.*;

public class ecommerce extends Thread {
	private ServerSocket serverSocket;
	private static String ivKey="0";
	private static crypto crypt = new crypto();

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

	private static String genSessKeyUser(Socket server) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		String msg1=get_msg(server);
		String brokerName = msg1.substring(0,msg1.length()-24);
		DatabaseConnectivity dbconn = new DatabaseConnectivity();
		Connection conn = dbconn.connectToDatabase();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select * from broker_details_amazon where broker_name = '" + brokerName.toLowerCase() + "'");
		String sessKey = crypt.genKey();
		if(rs.next()) {
			String secKey = rs.getString("user_secret_key");
			String userNonce = crypt.decrypt(secKey,ivKey,msg1.substring(msg1.length()-24,msg1.length()));
			String myNonce = Integer.toString(crypt.randInt(1, 1000));
			send_msg(server,crypt.encrypt(secKey,ivKey,sessKey+userNonce+myNonce));
			String msg3=crypt.decrypt(sessKey, ivKey,get_msg(server));
			if(!msg3.startsWith(myNonce)) {
				System.out.println("Nonces dont match for "+brokerName+".Exp:"+myNonce+"\tRxd:"+msg3.substring(0,myNonce.length()));
			}
		} else{
			System.out.println("\n Could not find user sec key \n");
		}
		return (sessKey);
	}

	public void run() {
		while(true) {
			try {
				Socket server = serverSocket.accept();
				String sessKey = genSessKeyUser(server);
				System.out.println("Session Key="+sessKey);
				server.close();
			} catch(SocketTimeoutException s) {
				System.out.println("Socket timed out!");
				break;
			} catch(IOException e) {
				System.out.println("Unexpected errror");
				break;
			} catch (InstantiationException|IllegalAccessException|ClassNotFoundException|SQLException e) {
				// TODO Auto-generated catch block
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