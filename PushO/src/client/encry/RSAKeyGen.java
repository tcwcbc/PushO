package client.encry;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

/**
 * 
 * @author 김재우
 *
 */
public class RSAKeyGen {

	private Key pubKey;
	private Key privKey;

	public RSAKeyGen() throws NoSuchAlgorithmException, NoSuchProviderException {
		SecureRandom random = new SecureRandom();

		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "SunJSSE"); // OK

		generator.initialize(2048, random); // 여기에서는 2048 bit 키를 생성하였음
		KeyPair pair = generator.generateKeyPair();
		pubKey = pair.getPublic(); // Kb(pub) 공개키
		privKey = pair.getPrivate();// Kb(pri) 개인키

	}

	// 공개키 반환
	public byte[] getPublicKey() {
		return pubKey.getEncoded();
	}

	// 비밀키 반환
	public byte[] getPrivateKey() {
		return privKey.getEncoded();
	}

}
