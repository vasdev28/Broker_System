import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.math.BigInteger;
import java.security.*;

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
import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

public class crypto {
	public static String shapeKey(String key) {
		return (key+"0000000000000000").substring(0,16);
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

	public String genKey() {
		String key = shapeKey(UUID.randomUUID().toString());
		return key;
	}

	public int randInt(int min,int max) {
		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}

	public static String[] privatePublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(1024);
		KeyPair keypair = keyGen.genKeyPair();
		PrivateKey privateKey = keypair.getPrivate();
		PublicKey publicKey = keypair.getPublic();
		String privatekey = privateKey.toString();
		String publickey = publicKey.toString();
		KeyFactory fact = KeyFactory.getInstance("RSA");
		RSAPublicKeySpec publ = fact.getKeySpec(keypair.getPublic(),
				RSAPublicKeySpec.class);
		RSAPrivateKeySpec priv = fact.getKeySpec(keypair.getPrivate(),
				RSAPrivateKeySpec.class);
		BigInteger privateExponent = priv.getPrivateExponent();
		BigInteger privateModulus = priv.getModulus();
		BigInteger publicExponent = publ.getPublicExponent();
		BigInteger publicModulus = publ.getModulus();
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(publicModulus, publicExponent);
		KeyFactory fact1 = KeyFactory.getInstance("RSA");
		PublicKey pubKey = fact1.generatePublic(keySpec);
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		//String src = "Data to be encrypted";
		byte[] cipherData = cipher.doFinal();

		System.out.println("The Private exponent is \n" + privateExponent + "\n The private modulus is \n" + privateModulus + "\n\n The Pubic Exponent is \n " + publicExponent + "\n The public Modulus is \n" + publicModulus);
		return new String[] {privatekey, publickey};
	}
}