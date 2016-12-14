package server.encry;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import server.res.ServerConst;


/**
 * RSA �Ϻ�ȣȭ�� ����ϸ� byte�迭�� ��ȯ�ϴµ� 
 * �̰��� Hex������ ��ȯ�� ���� ������ ��������
 * Hex���� byte�迭�� ��ȯ�Ͽ� ����Ѵ�.
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

	// 128��Ʈ Ű ����
	public static SecretKey get128bitKey() {
		KeyGenerator generator = null;
		SecureRandom random = null;
		try {
			generator = KeyGenerator.getInstance("AES");
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			ServerConst.SERVER_LOGGER.error(e.getMessage());
		}
		generator.init(128, random);
		SecretKey secureKey = generator.generateKey();
		
		return secureKey;
	}
	
}
