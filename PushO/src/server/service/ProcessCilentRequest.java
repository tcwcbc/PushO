package server.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import server.encry.AESUtils;
import server.exception.PushMessageSendingException;
import server.model.OrderInfo;
import server.model.PushInfo;
import server.res.ServerConst;
import server.util.ServerUtils;

/**
 * @author �ֺ�ö
 * @Description ������ �� Ŭ���̾�Ʈ�� ��������� �ϸ� �۾��� �����ϴ� ������
 * @TODO ������� ���� ��Ʈ���� ���ڽ�Ʈ��->����Ʈ��Ʈ�� ���� ��ȯ ������ ��ƿŬ���� ������� �޽��� �ۼ���� ���� Ÿ�Ӿƿ� ���ܰ�
 *       �߻����� ��� �˸��޽����� �����ϰ� �������� �� �ڿ�ȸ�� ��Ŀ���� ����ó������ ���� ���� try-catch���� ����
 */
public class ProcessCilentRequest extends Thread {

	private Socket connectedSocketWithClient;
	private byte[] msgPingByte;
	private byte[] msgPongByte;
	private byte[] msgPushByte;
	private boolean AuthSuccess = true;

	private BufferedOutputStream bos;
	private BufferedInputStream bis;


	private String aesKey;
	private ArrayList<String> orderNums = new ArrayList<String>();

	public LinkedBlockingQueue<String> receivedAckQueue;

	public ProcessCilentRequest(Socket socket, String aesKey, LinkedBlockingQueue<String> receivedAckQueue) {
		this.connectedSocketWithClient = socket;
		this.aesKey = aesKey;
		this.receivedAckQueue = receivedAckQueue;
		// ��Ʈ���� ���� Ÿ�Ӿƿ� ����
		try {
			connectedSocketWithClient.setSoTimeout(ServerConst.STREAM_TIME_OUT);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ServerConst.ACCESS_LOGGER.debug("ProcessClientRequest Created!");
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
//				ServerConst.MESSAGE_LOGGER.info("NACK Message Receive, orderNum:[{}]",response[2]);
			} catch (IOException e) {
				if(!orderNums.isEmpty()){
					this.receivedAckQueue.addAll(orderNums);
				}
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
				new byte[ServerConst.HEADER_LENTH + msgPingString.getBytes(ServerConst.CHARSET).length],
				msgPingString);

		byte[] header = new byte[ServerConst.HEADER_LENTH];
		byte[] body;
		int readCount = 0;
		int bodySize = 0;
		int bodylength = 0;
		bos.write(msgPingByte);
		bos.flush();
		ServerConst.MESSAGE_LOGGER.info("Send Message Authorization Complete, Msg:[{}]", msgPingString);

		while (!this.isInterrupted()) {
			readCount = bis.read(header);
			bodySize = ServerUtils.byteToInt(header);
			body = new byte[bodySize];
			bodylength = bis.read(body);

			String bodyDecodeMsg = AESUtils.AES_Decode(new String(body), aesKey);
			String msg = ServerUtils.parseJSONMessage(new JSONParser(), bodyDecodeMsg);

			if (msg.equals(ServerConst.JSON_VALUE_PONG)) {
				// TODO �ֹ������߼ۿ� ���� ������ ���� ��� �����ڿ��� receivedAckQueue�� �ִ� ����
				/*
				 * this.receivedAckQueue.put(new PushInfo());
				 */
				ServerConst.MESSAGE_LOGGER.info("Receive Message Authorization Complete, Msg:[{}]",msg);
			} else if (msg.contains(ServerConst.JSON_VALUE_PUSH_ORDER)) {
				String[] response = msg.split("/");
				/**
				 * ���� ���� response/fail or success/�ֹ���ȣ
				 */
				if (response[1].equals("success")) {
					ServerConst.MESSAGE_LOGGER.info("ACK Message Receive, orderNum:[{}]",response[2]);
					orderNums.add(response[2]);
				} else if (response[1].equals("fail")) {
					ServerConst.MESSAGE_LOGGER.info("NACK Message Receive, orderNum:[{}]",response[2]);
				}
			}
			if(!orderNums.isEmpty()){
				this.receivedAckQueue.addAll(orderNums);
			}
		}
	}

	/**
	 * Ŭ���̾�Ʈ���� �˸��� �����ϴ� �޼ҵ� String �����͸� �����ڷ� split�Ͽ� ������ ���Ŀ� �°� �����Ѵ�.
	 * 
	 * @param msg
	 *            �ֹ����� �޽���
	 * @throws PushMessageSendingException
	 *             ���� �� ��Ʈ���� ������� ������ �����Ǿ����� �����ϰ� Ǯ�� �ʿ��� ������ �˸��� ����
	 */
	public void setPushPartial(OrderInfo infoMsg) throws PushMessageSendingException {
		try {
			String msg = ServerUtils.makeJSONMessageForPush(infoMsg, new JSONObject(), new JSONObject());
			msg = AESUtils.AES_Encode(msg, aesKey);
			msgPushByte = ServerUtils.makeMessageStringToByte(
					new byte[ServerConst.HEADER_LENTH + msg.getBytes(ServerConst.CHARSET).length], msg);

			bos.write(msgPushByte);
			bos.flush();
			ServerConst.MESSAGE_LOGGER.info("Complete Sending Message : [{}] ",this.getName());
		} catch (IOException e) {
			// ��� Ŭ���̾�Ʈ ������ �������� �߻�
			// �׿� ���� HashMap�� ����Ǿ��ִ� ���� Thread�� ����� �۾��� �ʿ���
			this.interrupt();
			ServerConst.MESSAGE_LOGGER.info("Fail Sending Message : [{}] ",this.getName());
			throw new PushMessageSendingException(e);
		}
	}
	
	public void setPushAll(PushInfo pushInfo) throws PushMessageSendingException {
		try {
			String msg = ServerUtils.makeJSONMessageForPushAll(pushInfo, new JSONObject(), new JSONObject());
			msg = AESUtils.AES_Encode(msg, aesKey);
			msgPushByte = ServerUtils.makeMessageStringToByte(
					new byte[ServerConst.HEADER_LENTH + msg.getBytes(ServerConst.CHARSET).length], msg);

			bos.write(msgPushByte);
			bos.flush();
			ServerConst.MESSAGE_LOGGER.info("Complete Sending Message : [{}] ",this.getName());
		} catch (IOException e) {
			// ��� Ŭ���̾�Ʈ ������ �������� �߻�
			// �׿� ���� HashMap�� ����Ǿ��ִ� ���� Thread�� ����� �۾��� �ʿ���
			this.interrupt();
			ServerConst.MESSAGE_LOGGER.info("Fail Sending Message : [{}] ",this.getName());
			throw new PushMessageSendingException(e);
		}
	}
}
