import java.net.*;
import java.io.*;

public class user {
   private static String ivKey="0";
   private static String secKey="key123";
   private static crypto crypt = new crypto();
   
   private static String get_msg(Socket insock) {
		try {
	        DataInputStream in = new DataInputStream(insock.getInputStream());
	        String msg = in.readUTF();
			System.out.println("Client Rxd the msg: ["+msg+"]");
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
   
   public static void main(String [] args)  {
      String serverName = args[0];
      int port = Integer.parseInt(args[1]);
      try {
         Socket client = new Socket(serverName, port);
         int n=crypt.randInt(1,1000);
         send_msg(client,"Alice"+crypt.encrypt(secKey,ivKey,Integer.toString(n)));
         String dec_msg1=crypt.decrypt(secKey,ivKey,get_msg(client));
         send_msg(client,"No Mention!!!");
         client.close();
      } catch(IOException e) {
         e.printStackTrace();
      }
   }
}