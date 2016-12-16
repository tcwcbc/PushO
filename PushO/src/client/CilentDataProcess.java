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

	public static void sendAuth(BufferedOutputStream bos, String aesKey, int num) throws IOException {
		String msgAuthString = ClientUtils.makeJSONMessageForAuth("판매자"+num, "비밀번호~?", new JSONObject(), new JSONObject());
		msgAuthString = AESUtils.AES_Encode(msgAuthString, aesKey);
		byte[] msgAuthByte = ClientUtils.makeMessageStringToByte(
				new byte[ClientConst.HEADER_LENTH + msgAuthString.getBytes(ClientConst.CHARSET).length], msgAuthString);
		bos.write(msgAuthByte);
		bos.flush();
		ClientConst.CLIENT_LOGGER.info("인증 메시지 전송");
	}

	public static void sendPing(BufferedOutputStream bos, String aesKey) throws IOException {
		String msgPingString = ClientUtils.makeJSONMessageForPingPong(new JSONObject(), true);
		msgPingString = AESUtils.AES_Encode(msgPingString, aesKey);
		byte[] msgPingByte = ClientUtils.makeMessageStringToByte(
				new byte[ClientConst.HEADER_LENTH + msgPingString.getBytes(ClientConst.CHARSET).length], msgPingString);
		bos.write(msgPingByte);
		bos.flush();
	}

	public static void sendPong(BufferedOutputStream bos, String aesKey) throws IOException {
		String msgPongString = ClientUtils.makeJSONMessageForPingPong(new JSONObject(), false);
		msgPongString = AESUtils.AES_Encode(msgPongString, aesKey);
		byte[] msgPongByte = ClientUtils.makeMessageStringToByte(
				new byte[ClientConst.HEADER_LENTH + msgPongString.getBytes(ClientConst.CHARSET).length], msgPongString);
		bos.write(msgPongByte);
		ClientConst.CLIENT_LOGGER.info("pong 전송");
	}

	public static void receive(Socket socket, BufferedInputStream bis, BufferedOutputStream bos, String aesKey)
			throws IOException {
		int readCount;
		int dataSize;
		int bodyLength;
		boolean status = true;
		PushInfo pushData = null;

		socket.setSoTimeout(ClientConst.SEND_WATING_TIME);
		while (status) {
			while ((readCount = bis.read(header)) != -1) {
				// 수신된 메시지 DATASIZE
				dataSize = ClientUtils.byteToInt(header);
				// DATA 길이만큼 byte배열 선언
				byte[] body = new byte[dataSize];
				bodyLength = bis.read(body);
				String msg = ClientUtils.parseJSONMessage(new JSONParser(),
						AESUtils.AES_Decode(new String(body, ClientConst.CHARSET), aesKey));

				// ping 메시지 경우
				if (msg.equals(ClientConst.JSON_VALUE_PING)) {
					sendPong(bos, aesKey);
				}
				// pong 메시지 경우
				else if (msg.equals(ClientConst.JSON_VALUE_PONG)) {
					ClientConst.CLIENT_LOGGER.info("ACK 도착");
					status = false;
					break;
				}
				// Push 메시지 경우
				else if (msg.equals(ClientConst.JSON_VALUE_PUSH)) {
					pushData = ClientUtils.parsePushMessage(new JSONParser(),
							AESUtils.AES_Decode(new String(body, ClientConst.CHARSET), aesKey), pushData);
					ClientConst.CLIENT_LOGGER.info("주문번호" + pushData.getOrder_num() + " 알림이 도착했습니다");
				}

			} // end of while
		}
	}

	public static void occurTimeout(Socket socket, BufferedInputStream bis, BufferedOutputStream bos, String aesKey)
			throws IOException {
		ClientConst.CLIENT_LOGGER.error("Time out 발생...");
		ClientConst.CLIENT_LOGGER.info("ping 전송");
		sendPing(bos, aesKey);
		receive(socket, bis, bos, aesKey);
	}

}
