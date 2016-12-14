package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.res.ClientConst;
import client.util.ClientUtils;
import server.model.PushInfo;

public class CilentDataProcess {

	private static byte[] header = new byte[ClientConst.HEADER_LENTH];

	public static void sendAuth(BufferedOutputStream bos) throws IOException {
		String msgAuthString = ClientUtils.makeJSONMessageForAuth("판매자5", "비밀번호~?", new JSONObject(), new JSONObject());
		byte[] msgAuthByte = ClientUtils.makeMessageStringToByte(
				new byte[ClientConst.HEADER_LENTH + msgAuthString.getBytes(ClientConst.CHARSET).length], msgAuthString);
		bos.write(msgAuthByte);
		bos.flush();
	}

	public static void sendPing(BufferedOutputStream bos) throws IOException {
		String msgPingString = ClientUtils.makeJSONMessageForPingPong(new JSONObject(), true);
		byte[] msgPingByte = ClientUtils.makeMessageStringToByte(
				new byte[ClientConst.HEADER_LENTH + msgPingString.getBytes(ClientConst.CHARSET).length], msgPingString);
		bos.write(msgPingByte);
		bos.flush();
		System.out.println("ping 전송");
	}

	public static void sendPong(BufferedOutputStream bos) throws IOException {
		String msgPongString = ClientUtils.makeJSONMessageForPingPong(new JSONObject(), false);
		byte[] msgPongByte = ClientUtils.makeMessageStringToByte(
				new byte[ClientConst.HEADER_LENTH + msgPongString.getBytes(ClientConst.CHARSET).length], msgPongString);
		bos.write(msgPongByte);
		System.out.println("pong 전송");
	}

	public static void receive(Socket socket, BufferedInputStream bis, BufferedOutputStream bos) throws IOException {
		int readCount;
		int dataSize;
		int bodyLength;
		boolean status = true;
		PushInfo pushData = null;

		// 입력스트림에 대한 7초 타임아웃 설정
		socket.setSoTimeout(ClientConst.SEND_WATING_TIME);
		while (status) {
			while ((readCount = bis.read(header)) != -1) {
				// 수신된 메시지 DATASIZE
				dataSize = ClientUtils.byteToInt(header);
				// DATA 길이만큼 byte배열 선언
				byte[] body = new byte[dataSize];
				bodyLength = bis.read(body);
				String msg = ClientUtils.parseJSONMessage(new JSONParser(), new String(body, ClientConst.CHARSET));

				// ping 메시지 경우
				if (msg.equals(ClientConst.JSON_VALUE_PING)) {
					sendPong(bos);
				}
				// pong 메시지 경우
				else if (msg.equals(ClientConst.JSON_VALUE_PONG)) {
					System.out.println("ACK 도착");
					status = false;
					break;
				}
				// Push 메시지 경우
				else if (msg.equals(ClientConst.JSON_VALUE_PUSH)) {
					pushData = ClientUtils.parsePushMessage(new JSONParser(), new String(body, ClientConst.CHARSET),
							pushData);
					System.out.println(pushData.getOrder_list().get(0).getProduct().toString());
				}
			} // end of while
		}
	}

	public static void occurTimeout(Socket socket, BufferedInputStream bis, BufferedOutputStream bos)
			throws IOException {
		System.out.println("Time out 발생...");
		sendPing(bos);
		receive(socket, bis, bos);
	}

}
