package server.encry;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import server.res.ServerConst;

/**
 * RSA ��ȣȭ
 * 
 * @author �����
 * @Description DES256 privateKey�� Ŭ���̾�Ʈ���� ���� publicKey�� RSA��ȣȭ �Ѵ�.
 */
public class RSAEncryption {

	private byte[] rsa_publicKey;
	private String des_privateKey;

	/**
	 * 
	 * @param pubKey
	 *            Ŭ���̾�Ʈ���� ���� publicKey
	 * @param privateKey
	 *            �������� �����ϰ��ִ� DES privateKey
	 */
	public RSAEncryption(byte[] pubKey, String privateKey) {
		this.rsa_publicKey = pubKey;
		this.des_privateKey = privateKey;
	}

	/**
	 * 
	 * @return DES privateKey�� RSA publicKey�� ��ȣȭ�Ͽ� ��ȯ
	 * 
	 */
	public byte[] getkey() {
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING", "SunJCE");
			X509EncodedKeySpec ukeySpec = new X509EncodedKeySpec(rsa_publicKey);
			KeyFactory ukeyFactory = KeyFactory.getInstance("RSA");
			PublicKey publicKey = null;
			try {
				publicKey = ukeyFactory.generatePublic(ukeySpec);
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
				ServerConst.SERVER_LOGGER.error(e.getMessage());
			}

			// ����Ű�� �����Ͽ� ��ȣȭ
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] input = des_privateKey.getBytes();
			byte[] cipherText = cipher.doFinal(input);

			return cipherText;
		} catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException |
				InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			ServerConst.SERVER_LOGGER.error(e.getMessage());
		}
		return "����".getBytes();
	}
}
