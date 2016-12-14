package server.encry;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Hex;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import server.res.ServerConst;
import server.util.ServerUtils;

/**
 * 서버의 키교환이 이루어지는 클래스
 * 
 * @author 김재우
 *
 */
public class KeyExchangeServer {

	private BufferedInputStream bis;
	private BufferedOutputStream bos;

	private int readCount;
	private int dataSize;
	private int bodyLength;

	private byte[] receiveKey;
	private byte[] header;

	private String cipherMsg;
	private String aesKey;

	public KeyExchangeServer(Socket socket) {
		try {
			this.bis = new BufferedInputStream(socket.getInputStream());
			this.bos = new BufferedOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 서버로부터 데이터를 받는 메소드
	 * 
	 * @param index
	 *            인덱스 1 : 클라이언트로부터 RSA 공개키를 받을때 / 인덱스 2 : 클라이언트와 키 교환이 이루어지고
	 *            암호키로 제대로 전문이 암호화가 되는지 확인할때
	 * @throws IOException
	 */
	private void receiveForServer(int index) throws IOException {
		readCount = bis.read(header);
		dataSize = ServerUtils.byteToInt(header);
		byte[] body = new byte[dataSize];
		bodyLength = bis.read(body);
		String msg = ServerUtils.parseEncryMessage(new JSONParser(), new String(body, ServerConst.CHARSET));

		if (index == 1) {
			receiveKey = EncryUtils.hexToByteArray(msg);
		} else if (index == 2) {
			cipherMsg = AESUtils.AES_Decode(msg, aesKey);
		}
	}

	/**
	 * 클라이언트로부터 RSA 공개키를 받은후 서버가 가진 DES256 암호화키를 RSA공개키로 암호화 한 후에 서버로 보낸다
	 * 
	 * @throws IOException
	 */
	private void sendEncryDES256KeyToClient() throws IOException {
		aesKey = Hex.encodeHexString(EncryUtils.get128bitKey().getEncoded());
		RSAEncryption ec = new RSAEncryption(receiveKey, aesKey);
		System.out.println("AES키 생성:" + aesKey);
		String msgEncryString = ServerUtils.makeJSONMessageForEncry(EncryUtils.byteArrayToHex(ec.getkey()),
				new JSONObject(), new JSONObject());
		byte[] msgEncryByte = ServerUtils.makeMessageStringToByte(
				new byte[ServerConst.HEADER_LENTH + msgEncryString.getBytes(ServerConst.CHARSET).length],
				msgEncryString);
		bos.write(msgEncryByte);
		bos.flush();
	}

	/*
	 * 클라이언트와 암호화키를 교환한 후 클라이언트에서 Hello World를 암호화해서 전송이 왔을때 서버에서 암복호화가 제대로
	 * 이루어지는지 확인후 다시 리스폰스로 클라이언트에게 받은 문장을 암호화해서 보낸다.
	 */
	private void sendToClient() throws IOException {
		cipherMsg = AESUtils.AES_Encode(cipherMsg, aesKey);
		String msgEncryString = ServerUtils.makeJSONMessageForEncry(cipherMsg, new JSONObject(), new JSONObject());
		byte[] msgEncryByte = ServerUtils.makeMessageStringToByte(
				new byte[ServerConst.HEADER_LENTH + msgEncryString.getBytes(ServerConst.CHARSET).length],
				msgEncryString);
		bos.write(msgEncryByte);
		bos.flush();
	}

	public String start() {
		try {
			header = new byte[ServerConst.HEADER_LENTH];
			// 서버로 부터 RSA 공개키를 받는다
			receiveForServer(1);
			System.out.println("클라이언트의 RSA 공개키 받음");
			// 공개키로 암호화된 DES256 키를 보낸다
			sendEncryDES256KeyToClient();
			System.out.println("암호화된 AES암호키 전송");
			// 암호화된 Hello World 수신
			receiveForServer(2);
			System.out.println("암호키 테스트 'Hello World' 수신");
			// Hello World 복호화후 다시 암호화해서 전송
			sendToClient();
			System.out.println("암호키 테스트 'Hello World' 전송");

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return aesKey;
	}
}
