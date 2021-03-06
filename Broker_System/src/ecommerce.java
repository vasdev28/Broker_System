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
	private static String sb=null,sc=null,brokername = null,eComName;
	private static String upload_file = "tmp1.txt";

	public ecommerce(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(60000);
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
		Connection conn = dbconn.connectToDatabase("ecom");
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select * from broker_details_amazon where broker_name = '" + brokerName.toLowerCase() + "'");
		sb = crypt.genKey();
		if(rs.next()) {
			String secKey = rs.getString("shared_key");
			rs.close();
			stmt.close();
			conn.close();
			String userNonce = crypt.decrypt(secKey,ivKey,msg1.substring(msg1.length()-24,msg1.length()));
			String myNonce = Integer.toString(crypt.randInt(1, 1000));
			send_msg(server,crypt.encrypt(secKey,ivKey,sb+userNonce+myNonce));
			String msg3=crypt.decrypt(sb, ivKey,get_msg(server));
			if(!msg3.startsWith(myNonce)) {
				System.out.println("Nonces dont match for "+brokerName+".Exp:"+myNonce+"\tRxd:"+msg3.substring(0,myNonce.length()));
			}
		} else{
			rs.close();
			stmt.close();
			conn.close();
			System.out.println("\n Could not find user sec key \n");
		}
		brokername = brokerName;
	}

	private static void getSessKeyUser(Socket client) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		String msg1 = crypt.decrypt(sb, ivKey, get_msg(client));
		sc = crypt.RSADecrypt(eComName, msg1);
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

	private static int initiatePayment(Socket client,int itemNo) {
		DatabaseConnectivity dbconn = new DatabaseConnectivity();
		try {
		Connection conn = dbconn.connectToDatabase("ecom");
		Statement stmt = conn.createStatement();
		int numberItems=0;
		int itemPrice=65535;
		ResultSet rs3 = stmt.executeQuery("select * from vendor_inventory_amazon where item_no = " + itemNo);
		if(rs3.next()) {
			numberItems = Integer.parseInt(rs3.getString("number_items_avail"));
			itemPrice = Integer.parseInt(rs3.getString("item_price"));	
		}
		rs3.close();
		if(numberItems==0) {
			send_msg(client,crypt.encrypt(sb,ivKey,"Error: Items out-of-stock!!!"));
			stmt.close();
			conn.close();
			return 0;
		} else {
			int bill_no=1,numberOfItemsSold=1;
			ResultSet rs = stmt.executeQuery("select max(bill_no)+1 as max from broker_transaction_amazon");
			if (rs.next()) {
				try {
					bill_no = Integer.parseInt(rs.getString("max"));
				} catch (NumberFormatException e) {
					bill_no = 1;
				}
			}
			rs.close();
			int amount = itemPrice * numberOfItemsSold;
			send_msg(client,crypt.encrypt(sb, ivKey,"Bill," + crypt.encrypt(sc,ivKey,bill_no+",Give $"+itemPrice)+",$"+amount));

			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			String dateRxd = dateFormat.format(date);
			String queryInsertaftermsg1 = "insert into broker_transaction_amazon values(0," + bill_no + ",'" + brokername + "'," + amount + ",'" + dateRxd + "','ongoing','" + dateRxd + "')"; 
			stmt.executeUpdate(queryInsertaftermsg1);

			String msg4 = crypt.decrypt(sb,ivKey,get_msg(client));
			if (msg4.contains("Error")) {
				System.out.println("Payment Aborted by user due to insufficient credits");
				stmt.close();
				conn.close();
				return 0;
			}
			String msg4_reg[] = msg4.split("Paid .");
			String rxd_bill_no = crypt.decrypt(sc,ivKey,msg4_reg[0].substring(msg4_reg[0].length()-24,msg4_reg[0].length()));
			String order_num = msg4_reg[0].substring(0,msg4_reg[0].length()-24);
			String rxd_amt = msg4_reg[1];
			int orderNo = Integer.parseInt(order_num);

			String queryUpdateAfterRxdMsg = "update broker_transaction_amazon SET order_num = " + orderNo +", payment_rxd_date = '" + dateRxd + "', status = 'received' where bill_no = " + bill_no ;
			stmt.executeUpdate(queryUpdateAfterRxdMsg); 

			String queryinsertBillDetailsAmazon = "insert into bill_details_amazon values (" + bill_no + "," + numberOfItemsSold + "," + itemNo + ")";
			stmt.executeUpdate(queryinsertBillDetailsAmazon);
			String signature = crypt.RSASign(eComName, orderNo+",Rxd $"+rxd_amt);
			send_msg(client,crypt.encrypt(sb,ivKey,order_num+","+signature));
			
			String queryupdateAmazonInventoryNumberItems = "update vendor_inventory_amazon SET number_items_avail = number_items_avail - " + numberOfItemsSold + " , number_sold = number_sold + " + numberOfItemsSold + " where item_no = " + itemNo;
			stmt.executeUpdate(queryupdateAmazonInventoryNumberItems);
			stmt.close();
			conn.close();
			return 1;
		}
		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
			return 0;
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
				int itemRequested = sendInventory(server,"input.txt");
				System.out.println(itemRequested);
				if(initiatePayment(server,itemRequested)!=0) {
					send_file(server,upload_file);
					System.out.println("File transfer done");
				} else {
					System.out.println("Payment aborted");
				}
				server.close();
			} catch(SocketTimeoutException s) {
				System.out.println("Socket timed out!");
				break;
			} catch(InvalidKeyException | NoSuchAlgorithmException
					| InvalidKeySpecException | NoSuchPaddingException
					| IllegalBlockSizeException | BadPaddingException|IOException e) {
				System.out.println("Unexpected errror");
				e.printStackTrace();
				break;
			} catch (InstantiationException|IllegalAccessException|ClassNotFoundException|SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String [] args) {
		eComName = args[0];
		int port = Integer.parseInt(args[1]);
		try {
			Thread t = new ecommerce(port);
			t.start();
		} catch(Exception e1) {
			e1.printStackTrace();
		}
	}
}
