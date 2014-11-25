import javax.crypto.BadPaddingException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import java.util.ArrayList;

public class crypto {
	public static String shapeKey(String key) {
		return (key+"0000000000000000").substring(0,16);
	}
	
	public String genKey() {
		String key = shapeKey(UUID.randomUUID().toString());
		return key;
	}

	public int randInt(int min,int max) {
		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}

	public String encrypt(String key1, String key2, String value) {
		try {
			String secKey = shapeKey(key1);
			String ivKey = shapeKey(key2);
			IvParameterSpec iv = new IvParameterSpec(ivKey.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(secKey.getBytes("UTF-8"),"AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
			byte[] encrypted = cipher.doFinal(value.getBytes());
			return Base64.encodeBase64String(encrypted);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null; 
	}

	public ArrayList<String> encrypt(String key1, String key2, ArrayList<String> value) {
		ArrayList<String> enc_str = new ArrayList<String>();
		try {
			String secKey = shapeKey(key1);
			String ivKey = shapeKey(key2);
			IvParameterSpec iv = new IvParameterSpec(ivKey.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(secKey.getBytes("UTF-8"),"AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
			System.out.println("I/p List="+value);
			for(int i=0;i<value.size();i++) {
				String tmp = value.get(i);
				byte[] encrypted = cipher.doFinal(tmp.getBytes());
				enc_str.add(Base64.encodeBase64String(encrypted));
			}
			return enc_str;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null; 
	}
	
	public String decrypt(String key1, String key2, String encrypted) {
		try {
			String secKey = shapeKey(key1);
			String ivKey = shapeKey(key2);
			IvParameterSpec iv = new IvParameterSpec(ivKey.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(secKey.getBytes("UTF-8"),"AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));
			return new String(original);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public ArrayList<String> decrypt(String key1, String key2, ArrayList<String> encrypted) {
		ArrayList<String> dec_str =new ArrayList<String>();
		try {
			String secKey = shapeKey(key1);
			String ivKey = shapeKey(key2);
			IvParameterSpec iv = new IvParameterSpec(ivKey.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(secKey.getBytes("UTF-8"),"AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			for(int i=0;i<encrypted.size();i++) {
				byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted.get(i)));
				dec_str.add(new String(original));
			}
			return dec_str;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public void RSAkeyGen(String user) throws NoSuchAlgorithmException, InvalidKeySpecException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(1024);
		KeyPair keypair = keyGen.genKeyPair();
		KeyFactory fact = KeyFactory.getInstance("RSA");
		RSAPublicKeySpec publ = fact.getKeySpec(keypair.getPublic(),RSAPublicKeySpec.class);
		RSAPrivateKeySpec priv = fact.getKeySpec(keypair.getPrivate(),RSAPrivateKeySpec.class);

		BigInteger privateExponent = priv.getPrivateExponent();
		BigInteger privateModulus = priv.getModulus();
		BigInteger publicExponent = publ.getPublicExponent();
		BigInteger publicModulus = publ.getModulus();

		System.out.println("priEx= " + privateExponent + "\npriMod" + privateModulus + "\n pubExp = " + publicExponent + "\n pubMod = " + publicModulus );
		DatabaseConnectivity dbconn = new DatabaseConnectivity();
		Connection conn = dbconn.connectToDatabase();
		Statement stmt = conn.createStatement();
		stmt.executeUpdate("insert into public_key values ('" + user + "','" + publicExponent.toString() + "','" + publicModulus.toString() + "')");
		stmt.executeUpdate("insert into private_key values ('" + user + "','" + privateExponent.toString() + "','" + privateModulus.toString() + "')");		
	}

	public String RSAEncrypt(String user, String msg) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		String outp1 = null;
		DatabaseConnectivity dbconn = new DatabaseConnectivity();
		Connection conn = dbconn.connectToDatabase();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select * from public_key where user = '" + user + "'");
		if(rs.next()){
			BigInteger publicModulus = new BigInteger(rs.getString("public_modulus"));
			BigInteger publicExponent = new BigInteger(rs.getString("public_exponent"));	
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(publicModulus, publicExponent);
			KeyFactory fact1 = KeyFactory.getInstance("RSA");
			PublicKey pubKey = fact1.generatePublic(keySpec);
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			byte[] cipherData = cipher.doFinal(msg.getBytes());
			outp1 = Base64.encodeBase64String(cipherData);
		} else {
			System.out.println("The Public Key of user could not be found \n");
		}
		return outp1;
	}

	public String RSADecrypt(String user, String msg) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		String out2 = null;
		DatabaseConnectivity dbconn = new DatabaseConnectivity();
		Connection conn = dbconn.connectToDatabase();
		Statement stmt = conn.createStatement();
		ResultSet rs1 = stmt.executeQuery("select * from private_key where user = '" + user + "'");
		if(rs1.next()){
			BigInteger privateModulus = new BigInteger(rs1.getString("private_modulus"));
			BigInteger privateExponent = new BigInteger(rs1.getString("private_exponent"));
			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(privateModulus, privateExponent);
			KeyFactory fact2 = KeyFactory.getInstance("RSA");
			PrivateKey privKey = fact2.generatePrivate(keySpec);
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			byte[] cipherData = cipher.doFinal(Base64.decodeBase64(msg));
			out2 = new String(cipherData);
		} else {
			System.out.println("The Private key of user could not be found \n");
		}
		return out2;
	}
}