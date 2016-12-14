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
 * RSA ��ȣȭ
 * 
 * @author �����
 * @Description �������� DES256 ���Ű�� RSA ����Ű�� ��ȣȭ�Ͽ� �����ϸ� RSA ���Ű�� ��ȣȭ�Ͽ�
 *              DES256 Ű�� ��´�.
 */
public class RSADecryption {

	private byte[] cipherText;
	private byte[] des_privateKey;

	/**
	 * 
	 * @param cipherText
	 *            �������� ���� ��ȣ��
	 * @param privKeyStr
	 *            Ŭ���̾�Ʈ���� �߱��� RSA���Ű
	 */
	public RSADecryption(byte[] cipherText, byte[] des_privateKey) {
		this.cipherText = cipherText;
		this.des_privateKey = des_privateKey;
	}

	/**
	 * �������� ���� ��ȣ���� ��ȣȭ�ؼ� ������ DES��ȣŰ�� �����ϰ� �ȴ�.
	 * 
	 * @return DES�˰��� ���Ű
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 */
	public String getDESkey() throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException,
			NoSuchProviderException, NoSuchPaddingException, InvalidKeyException {

		// ����Ű�� ��� PKCS#8 Ÿ������ ���ڵ� �Ǿ��ִ�.
		// PKCS8EncodedKeySpec Ŭ������ �̿��Ͽ� �ε�������Ѵ�.
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING", "SunJCE");
		PKCS8EncodedKeySpec rkeySpec = new PKCS8EncodedKeySpec(des_privateKey);

		KeyFactory rkeyFactory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = null;
		try {
			privateKey = rkeyFactory.generatePrivate(rkeySpec);
		} catch (InvalidKeySpecException e) {
			ClientConst.CLIENT_LOGGER.error(e.getMessage());
		}

		// ����Ű�� �������ִ��ʿ��� ��ȣȭ
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] plainText = cipher.doFinal(cipherText);

		return new String(plainText);
	}
}
