package test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BlockingQueueTest {
	public static void main(String[] args) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(("daoutest1" + "sadsad").getBytes());
			
			byte byteData[] = md.digest();
			
			StringBuffer sb = new StringBuffer();
			for(int i = 0; i<byteData.length; i++) {
				sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
			}
			String retVal = sb.toString();
			System.out.println(retVal);
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		
		}	
	}
}
