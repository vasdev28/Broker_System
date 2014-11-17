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
         String sessKey=dec_msg1.substring(0, 16);
         int n_len = Integer.toString(n).length();
         String n1=dec_msg1.substring(16,16+n_len);
         if(Integer.parseInt(n1)!=n) {
        	 System.out.println("Aborting connection since Nonces don't match:Expected"+n1+"Received"+dec_msg1.substring(16,16+n_len));
        	 client.close();
         } else {
        	 String n2=dec_msg1.substring(16+n_len, dec_msg1.length());
        	 send_msg(client,crypt.encrypt(sessKey, ivKey, n2+"Amazon"));
        	 System.out.println("Nonces match"+n1+"Generated"+n2);
         }
         client.close();
      } catch(IOException e) {
         e.printStackTrace();
      }
   }
}