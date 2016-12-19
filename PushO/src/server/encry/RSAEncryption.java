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
 * RSA 암호화
 * 
 * @author 김재우
 * @Description DES256 privateKey를 클라이언트에서 받은 publicKey로 RSA암호화 한다.
 */
public class RSAEncryption {

	private byte[] rsa_publicKey;
	private String des_privateKey;

	/**
	 * 
	 * @param pubKey
	 *            클라이언트에게 받은 publicKey
	 * @param privateKey
	 *            서버에서 저장하고있는 DES privateKey
	 */
	public RSAEncryption(byte[] pubKey, String privateKey) {
		this.rsa_publicKey = pubKey;
		this.des_privateKey = privateKey;
	}

	/**
	 * 
	 * @return DES privateKey를 RSA publicKey로 암호화하여 반환
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
				ServerConst.ACCESS_LOGGER.error(e.getMessage());
			}

			// 공개키를 전달하여 암호화
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] input = des_privateKey.getBytes();
			byte[] cipherText = cipher.doFinal(input);

			return cipherText;
		} catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException |
				InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			e.getStackTrace();
			ServerConst.ACCESS_LOGGER.error(e.getMessage());
		}
		return "에러".getBytes();
	}
}
