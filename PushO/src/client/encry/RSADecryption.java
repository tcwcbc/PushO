package client.encry;


import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import client.res.ClientConst;

/**
 * RSA 복호화
 * 
 * @author 김재우
 * @Description 서버에서 DES256 비밀키를 RSA 공개키로 암호화하여 전달하면 RSA 비밀키로 복호화하여
 *              DES256 키를 얻는다.
 */
public class RSADecryption {

	private byte[] cipherText;
	private byte[] des_privateKey;

	/**
	 * 
	 * @param cipherText
	 *            서버에서 받은 암호문
	 * @param privKeyStr
	 *            클라이언트에서 발급한 RSA비밀키
	 */
	public RSADecryption(byte[] cipherText, byte[] des_privateKey) {
		this.cipherText = cipherText;
		this.des_privateKey = des_privateKey;
	}

	/**
	 * 서버에서 받은 암호문을 복호화해서 서버와 DES암호키를 공유하게 된다.
	 * 
	 * @return DES알고리즘 비밀키
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 */
	public String getDESkey() throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException,
			NoSuchProviderException, NoSuchPaddingException, InvalidKeyException {

		// 개인키의 경우 PKCS#8 타입으로 인코딩 되어있다.
		// PKCS8EncodedKeySpec 클래스를 이용하여 로딩해줘야한다.
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING", "SunJCE");
		PKCS8EncodedKeySpec rkeySpec = new PKCS8EncodedKeySpec(des_privateKey);

		KeyFactory rkeyFactory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = null;
		try {
			privateKey = rkeyFactory.generatePrivate(rkeySpec);
		} catch (InvalidKeySpecException e) {
			ClientConst.CLIENT_LOGGER.error(e.getMessage());
		}

		// 개인키를 가지고있는쪽에서 복호화
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] plainText = cipher.doFinal(cipherText);

		return new String(plainText);
	}
}
