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
 * ������ Ű��ȯ�� �̷������ Ŭ����
 * 
 * @author �����
 *
 */
public class KeyExchangeClient {

	private BufferedInputStream bis;
	private BufferedOutputStream bos;

	private int readCount;
	private int dataSize;
	private int bodyLength;
	// ������ ���� ���� DES256 Ű
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
			ClientConst.CLIENT_LOGGER.error(e.getMessage());
			// RSA Ű������ ����
		}
	}

	/**
	 * 
	 * @param index
	 *            �ε��� 1 : �������� RSA ����Ű �����Ҷ� 
	 *            �ε��� 2 : Ű ��ȯ�� �̷������ Hello World �׽�Ʈ
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
		// Ű ��ȯ�� �̷������ ������ȣȭ �׽�Ʈ
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
	 *            �ε��� 1 : �����κ��� RSA ����Ű�� ��ȣȭ�� DES256 ���Ű�� ������ 
	 *            �ε��� 2 : �����κ��� Hello World �׽�Ʈ�� ���� ���������� ������
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
			//���� �����Ϳ� ���� �����Ͱ� �ٸ��ٸ� Ű��ȯ�� ������ �ִٰ� �Ǵ��ϴ�
			//������ �ʿ��ϴ�.
		}

	}

	private void decryptionAes256Key() {
		try {
			receiveKey = EncryUtils.hexToByteArray(cipherKey);
			RSADecryption rd = new RSADecryption(receiveKey, privKey);
			desKey = rd.getDESkey();
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
				| NoSuchProviderException | NoSuchPaddingException e) {
			ClientConst.CLIENT_LOGGER.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public String start() {
		try {
			// ��� ����
			header = new byte[ClientConst.HEADER_LENTH];
			// Ű ������ �ʱ�ȭ
			initialize();
			ClientConst.CLIENT_LOGGER.info("RSA Ű ����");
			// ������ RSA ����Ű ����
			sendToServer(1);
			ClientConst.CLIENT_LOGGER.info("RSA ����Ű ����");
			// ������ ���� RSA ����Ű�� ��ȣȭ�� AES256 ���Ű ����
			receiveForServer(1);
			ClientConst.CLIENT_LOGGER.info("�����κ��� AES256 ��ȣȭŰ ����");
			// ��ȣȭ�� DES256 ���Ű ��ȣȭ
			decryptionAes256Key();
			// Hello World ��ȣ�� ����
			sendToServer(2);
			ClientConst.CLIENT_LOGGER.info("��ȣŰ �׽�Ʈ 'Hello World'����");
			// Hello World ��ȣ�� ���� �ޱ�
			receiveForServer(2);
			ClientConst.CLIENT_LOGGER.info("��ȣŰ �׽�Ʈ 'Hello World'����");
		} catch (IOException e) {
			ClientConst.CLIENT_LOGGER.error(e.getMessage());
		} catch (Throwable e) {
			ClientConst.CLIENT_LOGGER.error(e.getMessage());
			// desŰ ���� ����
		}
		return desKey;
	}

}
