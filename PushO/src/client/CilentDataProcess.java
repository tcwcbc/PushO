package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.encry.AESUtils;
import client.res.ClientConst;
import client.util.ClientUtils;
import server.model.PushInfo;

public class CilentDataProcess {

	private static byte[] header = new byte[ClientConst.HEADER_LENTH];

	public static void sendAuth(BufferedOutputStream bos, String aesKey) throws IOException {
		String msgAuthString = ClientUtils.makeJSONMessageForAuth("�Ǹ���5", "��й�ȣ~?", new JSONObject(), new JSONObject());
		msgAuthString = AESUtils.AES_Encode(aesKey, msgAuthString);
		byte[] msgAuthByte = ClientUtils.makeMessageStringToByte(
				new byte[ClientConst.HEADER_LENTH + msgAuthString.getBytes(ClientConst.CHARSET).length], msgAuthString);
		bos.write(msgAuthByte);
		bos.flush();
	}

	public static void sendPing(BufferedOutputStream bos, String aesKey) throws IOException {
		String msgPingString = ClientUtils.makeJSONMessageForPingPong(new JSONObject(), true);
		msgPingString = AESUtils.AES_Encode(aesKey, msgPingString);
		byte[] msgPingByte = ClientUtils.makeMessageStringToByte(
				new byte[ClientConst.HEADER_LENTH + msgPingString.getBytes(ClientConst.CHARSET).length], msgPingString);
		bos.write(msgPingByte);
		bos.flush();
		System.out.println("ping ����");
	}

	public static void sendPong(BufferedOutputStream bos, String aesKey) throws IOException {
		String msgPongString = ClientUtils.makeJSONMessageForPingPong(new JSONObject(), false);
		msgPongString = AESUtils.AES_Encode(aesKey, msgPongString);
		byte[] msgPongByte = ClientUtils.makeMessageStringToByte(
				new byte[ClientConst.HEADER_LENTH + msgPongString.getBytes(ClientConst.CHARSET).length], msgPongString);
		bos.write(msgPongByte);
		System.out.println("pong ����");
	}

	public static void receive(Socket socket, BufferedInputStream bis, BufferedOutputStream bos, String aesKey)
			throws IOException {
		int readCount;
		int dataSize;
		int bodyLength;
		boolean status = true;
		PushInfo pushData = null;

		// �Է½�Ʈ���� ���� 7�� Ÿ�Ӿƿ� ����
		socket.setSoTimeout(ClientConst.SEND_WATING_TIME);
		while (status) {
			while ((readCount = bis.read(header)) != -1) {
				// ���ŵ� �޽��� DATASIZE
				dataSize = ClientUtils.byteToInt(header);
				// DATA ���̸�ŭ byte�迭 ����
				byte[] body = new byte[dataSize];
				bodyLength = bis.read(body);
				String msg = ClientUtils.parseJSONMessage(new JSONParser(),
						AESUtils.AES_Decode(new String(body, ClientConst.CHARSET), aesKey));

				// ping �޽��� ���
				if (msg.equals(ClientConst.JSON_VALUE_PING)) {
					sendPong(bos, aesKey);
				}
				// pong �޽��� ���
				else if (msg.equals(ClientConst.JSON_VALUE_PONG)) {
					System.out.println("ACK ����");
					status = false;
					break;
				}
				// Push �޽��� ���
				else if (msg.equals(ClientConst.JSON_VALUE_PUSH)) {
					pushData = ClientUtils.parsePushMessage(new JSONParser(),
							AESUtils.AES_Decode(new String(body, ClientConst.CHARSET), aesKey), pushData);
					System.out.println("�ֹ���ȣ" + pushData.getOrder_num() + " �˸��� �����߽��ϴ�");
				}
			} // end of while
		}
	}

	public static void occurTimeout(Socket socket, BufferedInputStream bis, BufferedOutputStream bos, String aesKey)
			throws IOException {
		System.out.println("Time out �߻�...");
		sendPing(bos, aesKey);
		receive(socket, bis, bos, aesKey);
	}

}
