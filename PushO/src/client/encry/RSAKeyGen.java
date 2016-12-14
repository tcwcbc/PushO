package client.encry;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

/**
 * RSA ����Ű, ����Ű ����
 * @author �����
 *
 */
public class RSAKeyGen {

	private Key pubKey;
	private Key privKey;

	public RSAKeyGen() throws NoSuchAlgorithmException, NoSuchProviderException {
		SecureRandom random = new SecureRandom();

		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "SunJSSE"); // OK

		generator.initialize(2048, random); // ���⿡���� 2048 bit Ű�� �����Ͽ���
		KeyPair pair = generator.generateKeyPair();
		pubKey = pair.getPublic(); // Kb(pub) ����Ű
		privKey = pair.getPrivate();// Kb(pri) ����Ű

	}

	// ����Ű ��ȯ
	public byte[] getPublicKey() {
		return pubKey.getEncoded();
	}

	// ���Ű ��ȯ
	public byte[] getPrivateKey() {
		return privKey.getEncoded();
	}

}
