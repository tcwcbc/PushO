package server.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import server.encry.AESUtils;
import server.exception.PushMessageSendingException;
import server.res.ServerConst;
import server.util.ServerUtils;

/**
 * @author �ֺ�ö
 * @Description ������ �� Ŭ���̾�Ʈ�� ��������� �ϸ� �۾��� �����ϴ� ������
 * @TODO ������� ���� ��Ʈ���� ���ڽ�Ʈ��->����Ʈ��Ʈ�� ���� ��ȯ ������ ��ƿŬ���� ������� �޽��� �ۼ���� ���� Ÿ�Ӿƿ� ���ܰ�
 *       �߻����� ��� �˸��޽����� �����ϰ� �������� �� �ڿ�ȸ�� ��Ŀ���� ����ó������ ���� ���� try-catch���� ����
 */
public class ProcessCilentRequest extends Thread{

	private Socket connectedSocketWithClient;
	private byte[] msgPingByte;
	private byte[] msgPongByte;
	private byte[] msgPushByte;
	private boolean AuthSuccess = true;

	private BufferedOutputStream bos;
	private BufferedInputStream bis;
	
	private String aesKey;

	public ProcessCilentRequest(Socket socket, String aesKey) {
		this.connectedSocketWithClient = socket;
		this.aesKey = aesKey;
	}

	@Override
	public void run() {
		try {
			bos = new BufferedOutputStream(connectedSocketWithClient.getOutputStream());
			bis = new BufferedInputStream(connectedSocketWithClient.getInputStream());

			/**
			 * ���� �����͸� �ְ� �޴� �κ�
			 * 
			 * @TODO : �����Ӹ� �ƴ϶� ���� �����͸� �ְ�޴� ����
			 */
			if (AuthSuccess) {
				sendSuccessMsg();
				AuthSuccess = false;
			}
		} catch (SocketTimeoutException timeoutE) {
			// TODO Ÿ�Ӿƿ� �߻� �ÿ� �ڿ�ȸ�� �� ó�� ��Ŀ����
			timeoutE.printStackTrace();
			try {
				bos.write(msgPingByte);
				System.out.println("Ping ����");

			} catch (IOException e) {
				e.printStackTrace();
				try {
					bis.close();
					bos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void sendSuccessMsg() throws IOException, InterruptedException {
		String msgPingString = ServerUtils.makeJSONMessageForPingPong(new JSONObject(), false);
		msgPingString = AESUtils.AES_Encode(msgPingString, aesKey);
		msgPingByte = ServerUtils.makeMessageStringToByte(
				new byte[ServerConst.HEADER_LENTH + msgPingString.getBytes().length], msgPingString);

		byte[] header = new byte[ServerConst.HEADER_LENTH];
		byte[] body;
		int readCount = 0;
		int bodySize = 0;
		int bodylength = 0;

		bos.write(msgPingByte);
		bos.flush();
		System.out.println("���� ���� �޽��� ����");

		while (!this.isInterrupted()) {
			readCount = bis.read(header);
			bodySize = ServerUtils.byteToInt(header);
			body = new byte[bodySize];
			bodylength = bis.read(body);
			String msg = ServerUtils.parseJSONMessage(new JSONParser(), AESUtils.AES_Decode(new String(body), aesKey));

			if (msg.equals(ServerConst.JSON_VALUE_PONG)) {
				System.out.println("ACK");
			}
			bos.flush();
		}
	}

	/**
	 * Ŭ���̾�Ʈ���� �˸��� �����ϴ� �޼ҵ� String �����͸� �����ڷ� split�Ͽ� ������ ���Ŀ� �°� �����Ѵ�.
	 * @param msg		�ֹ����� �޽���
	 * @throws PushMessageSendingException ���� �� ��Ʈ���� ������� ������ �����Ǿ����� �����ϰ� Ǯ�� �ʿ��� ������ �˸��� ����
	 */
	public void setPush(String msg) throws PushMessageSendingException {
		try {
			msg = AESUtils.AES_Encode(msg, aesKey);
			msgPushByte = ServerUtils.makeMessageStringToByte(
					new byte[ServerConst.HEADER_LENTH + msg.getBytes(ServerConst.CHARSET).length], msg);

			bos.write(msgPushByte);
			bos.flush();
			System.out.println("Ǫ���Ϸ�:" + this.getName());
		} catch (IOException e) {
			// ��� Ŭ���̾�Ʈ ������ �������� �߻�
			// �׿� ���� HashMap�� ����Ǿ��ִ� ���� Thread�� ����� �۾��� �ʿ���
			System.out.println("setPush() Ǫ���߼��� ����" + e.getMessage());
			this.interrupt();
			throw new PushMessageSendingException(e);
		}
	}
}
