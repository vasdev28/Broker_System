import java.net.*;
import java.io.*;

public class broker extends Thread {
	private ServerSocket serverSocket;
	private static String ivKey="0";
	private static String secKey="a6113f9c-0643-43";
	private static crypto crypt = new crypto();

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

	private static String genSessKeyUser(Socket server) throws IOException {
		String msg1=get_msg(server);
		String user = msg1.substring(0,msg1.length()-24);
		String userNonce = crypt.decrypt(secKey,ivKey,msg1.substring(msg1.length()-24,msg1.length()));
		String myNonce = Integer.toString(crypt.randInt(1, 1000));
		String sessKey = crypt.genKey();
		send_msg(server,crypt.encrypt(secKey,ivKey,sessKey+userNonce+myNonce));
		String msg3=crypt.decrypt(sessKey, ivKey,get_msg(server));
		if(!msg3.startsWith(myNonce)) {
			System.out.println("Nonces dont match for "+user+".Exp:"+myNonce+"\tRxd:"+msg3.substring(0,myNonce.length()));
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
			}
		}
	}

	public static void main(String [] args) {
		int port = Integer.parseInt(args[0]);
		try {
			Thread t = new broker(port);
			t.start();
		} catch(Exception e1) {
			e1.printStackTrace();
		}
	}
}