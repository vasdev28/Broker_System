import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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
}