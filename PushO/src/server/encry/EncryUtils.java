package server.encry;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import server.res.ServerConst;


/**
 * RSA 암복호화를 사용하면 byte배열로 반환하는데 
 * 이것을 Hex값으로 변환한 다음 보내고 받을때는
 * Hex값을 byte배열로 변환하여 사용한다.
 * @author user
 *
 */
public class EncryUtils {

	// hex string to byte[]
	public static byte[] hexToByteArray(String hex) {
		if (hex == null || hex.length() == 0) {
			return null;
		}
		byte[] ba = new byte[hex.length() / 2];
		for (int i = 0; i < ba.length; i++) {
			ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return ba;
	}

	// byte[] to hex sting
	public static String byteArrayToHex(byte[] ba) {
		if (ba == null || ba.length == 0) {
			return null;
		}
		StringBuffer sb = new StringBuffer(ba.length * 2);
		String hexNumber;
		for (int x = 0; x < ba.length; x++) {
			hexNumber = "0" + Integer.toHexString(0xff & ba[x]);

			sb.append(hexNumber.substring(hexNumber.length() - 2));
		}
		return sb.toString();
	}

	// 128비트 키 생성
	public static SecretKey get128bitKey() {
		KeyGenerator generator = null;
		SecureRandom random = null;
		try {
			generator = KeyGenerator.getInstance("AES");
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			ServerConst.SERVER_LOGGER.error("AES 키 생성 에러 " + e.getMessage());
		}
		generator.init(128, random);
		SecretKey secureKey = generator.generateKey();
		
		return secureKey;
	}
	
}
