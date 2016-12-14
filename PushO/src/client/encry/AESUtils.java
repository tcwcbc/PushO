package client.encry;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import client.res.ClientConst;


/**
 * 
 * 전문을 주고받을때 암호화하고 복호화하기 위한 Utill 클래스
 * 사전에 키 교환 작업이 이루어저야 사용 가능
 * @author user
 *
 */
public class AESUtils {

	public static byte[] ivBytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00 };

	/**
	 * 
	 * @param str
	 *            암호화 하고자 하는 평문
	 * @param key
	 *            서버와 공유한 암호키
	 * @return
	 * 
	 */
	public static String AES_Encode(String str, String key) {
		Cipher cipher = null;
		String result = null;
		byte[] textBytes;
		try {
			textBytes = str.getBytes("UTF-8");
			AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
			SecretKeySpec newKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);

			result = Base64.encodeBase64String(cipher.doFinal(textBytes));
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
				| UnsupportedEncodingException e) {
			ClientConst.CLIENT_LOGGER.error(e.getMessage());
		}

		return result;
	}

	/**
	 * 
	 * @param str
	 *            복호화 하고자 하는 암호문
	 * @param key
	 *            서버와 공유한 암호키
	 * @return
	 * 
	 */
	public static String AES_Decode(String str, String key) {
		String result = null;
		try {
			byte[] textBytes = Base64.decodeBase64(str);
			// byte[] textBytes = str.getBytes("UTF-8");
			AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
			SecretKeySpec newKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);

			result = new String(cipher.doFinal(textBytes), "UTF-8");
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
				| UnsupportedEncodingException e) {
			ClientConst.CLIENT_LOGGER.error(e.getMessage());
		}

		return result;
	}

}
