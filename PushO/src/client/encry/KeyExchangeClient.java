package client.encry;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.res.ClientConst;
import client.util.ClientUtils;

/**
 * 서버의 키교환이 이루어지는 클래스
 * 
 * @author 김재우
 *
 */
public class KeyExchangeClient {

	private BufferedInputStream bis;
	private BufferedOutputStream bos;

	private int readCount;
	private int dataSize;
	private int bodyLength;
	// 서버로 부터 받은 DES256 키
	private String desKey = null;
	private String cipherKey;

	private byte[] pubKey;
	private byte[] privKey;
	private byte[] receiveKey;
	private byte[] header;

	public KeyExchangeClient(BufferedInputStream bis, BufferedOutputStream bos) {
		this.bis = bis;
		this.bos = bos;
	}

	public void initialize() {
		try {
			RSAKeyGen encKey = new RSAKeyGen();
			pubKey = encKey.getPublicKey();
			privKey = encKey.getPrivateKey();
		} catch (Throwable e) {
			System.out.println(e.getMessage());
			// RSA 키생성중 에러
		}
	}

	/**
	 * 
	 * @param index
	 *            인덱스 1 : 서버에게 RSA 공개키 전송할때 
	 *            인덱스 2 : 키 교환이 이루어지고 Hello World 테스트
	 *            
	 */
	private void sendToServer(int index) throws IOException {
		String msgEncryString = null;
		String hexMsg = null;
		if (index == 1) {
			hexMsg = EncryUtils.byteArrayToHex(pubKey);
			msgEncryString = ClientUtils.makeJSONMessageForEncry(hexMsg, new JSONObject(),
					new JSONObject());
		}
		// 키 교환이 이루어지고 전문암호화 테스트
		else if (index == 2) {
			hexMsg = AESUtils.AES_Encode("Hello World", desKey);
			msgEncryString = ClientUtils.makeJSONMessageForEncry(hexMsg,
					new JSONObject(), new JSONObject());
		}

		byte[] msgEncryByte = ClientUtils.makeMessageStringToByte(
				new byte[ClientConst.HEADER_LENTH + msgEncryString.getBytes(ClientConst.CHARSET).length],
				msgEncryString);
		bos.write(msgEncryByte);
		bos.flush();

	}

	/**
	 * 
	 * @param index
	 *            인덱스 1 : 서버로부터 RSA 공개키로 암호화된 DES256 비밀키를 받을때 
	 *            인덱스 2 : 서버로부터 Hello World 테스트에 대한 리스폰스를 받을때
	 *            
	 */
	private void receiveForServer(int index) throws IOException {
		readCount = bis.read(header);
		dataSize = ClientUtils.byteToInt(header);
		byte[] body = new byte[dataSize];
		bodyLength = bis.read(body);

		if (index == 1) {
			cipherKey = ClientUtils.parseEncryMessage(new JSONParser(), new String(body, ClientConst.CHARSET));
		} else if (index == 2) {
			String msg = ClientUtils.parseEncryMessage(new JSONParser(), new String(body, ClientConst.CHARSET));
			//보낸 데이터와 받은 데이터가 다르다면 키교환에 문제가 있다고 판단하는
			//로직이 필요하다.
		}

	}

	private void decryptionAes256Key() {
		try {
			receiveKey = EncryUtils.hexToByteArray(cipherKey);
			RSADecryption rd = new RSADecryption(receiveKey, privKey);
			desKey = rd.getDESkey();
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
				| NoSuchProviderException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String start() {
		try {
			// 헤더 정의
			header = new byte[ClientConst.HEADER_LENTH];
			// 키 생성후 초기화
			initialize();
			System.out.println("RSA 키 생성");
			// 서버로 RSA 공개키 보냄
			sendToServer(1);
			System.out.println("RSA 공개키 전송");
			// 서버로 부터 RSA 공개키로 암호화된 AES256 비밀키 받음
			receiveForServer(1);
			System.out.println("서버로부터 AES256 암호화키 받음");
			// 암호화된 DES256 비밀키 복호화
			decryptionAes256Key();
			// Hello World 암호문 전송
			sendToServer(2);
			System.out.println("암호키 테스트 'Hello World'전송");
			// Hello World 암호문 응답 받기
			receiveForServer(2);
			System.out.println("암호키 테스트 'Hello World'받음");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (Throwable e) {
			System.out.println(e.getMessage());
			// des키 추출 에러
		}
		return desKey;
	}

}
