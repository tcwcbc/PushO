package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.res.ClientConst;
import server.model.PushInfo;
import server.service.OIOServer;
import server.util.ServerUtils;

/**
 * @author 최병철
 * @Description 클라이언트 프로그램, 생성과 동시에 서버소켓으로 접속 후 시나리오에 따라 통신 TODO 송수신 하는 스트림을
 *              문자스트림에서 바이트스트림으로 변환 타임아웃이나 기타 연결문제 복구 매커니즘 구현 및 기타 예외처리 이벤트를
 *              발생시키거나 데이터를 수신 후 가시적으로 보여줄 View
 */
public class OIOClient {
	private Socket socket;
	private BufferedOutputStream bos;
	private BufferedInputStream bis;

	private PushInfo pushData;

	// 서버에 연결 작업
	public boolean connectServer() {
		try {
			socket = new Socket(ClientConst.SERVER_IP, ClientConst.PORT_NUM);
			
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());

			// 인증을 위한 JSON 메세지 생성
			String msgAuthString = ServerUtils.makeJSONMessageForAuth("다우마트구매자", "비밀번호~?", new JSONObject(), new JSONObject());
			byte[] msgAuthByte = ServerUtils.makeMessageStringToByte(
					new byte[ClientConst.HEADER_LENTH + msgAuthString.getBytes(ClientConst.CHARSET).length], msgAuthString);
			bos.write(msgAuthByte);
			bos.flush();
			System.out.println("인증 보냄");
			return true;
		} catch (IOException e) {
			System.out.println("connectServer() Exception 발생!!");
			return false;
		}
	}

	// 메시지 송수신 메소드
	public void processMsg() {
		boolean status = true;
		int readCount = 0;
		int headerLength = 0;
		int bodyLength = 0;

		// 수신된 메시지 DATASIZE
		byte[] header = new byte[ClientConst.HEADER_LENTH];

		// 실제 데이터를 주고 받는 부분 TODO : 핑퐁뿐만 아니라 실제 데이터를 주고받는 로직
		while (status) {
			try {
				// 입력스트림에 대한 타임아웃 설정
//				socket.setSoTimeout(Const.SEND_WATING_TIME);
				while ((readCount = bis.read(header)) != -1) {
					// 수신된 메시지 DATASIZE
					headerLength = ServerUtils.byteToInt(header);
					// DATA 길이만큼 byte배열 선언
					byte[] body = new byte[headerLength];
					bodyLength = bis.read(body);
					String msg = ServerUtils.parseJSONMessage(new JSONParser(), new String(body, ClientConst.CHARSET));

					// Ping 메시지 일 경우
					if (msg.equals(ClientConst.JSON_VALUE_PING)) {
						String msgPongString = ServerUtils.makeJSONMessageForPingPong(new JSONObject(), false);
						byte[] msgPongByte = ServerUtils.makeMessageStringToByte(
								new byte[ClientConst.HEADER_LENTH + msgPongString.getBytes(ClientConst.CHARSET).length],
								msgPongString);
						bos.write(msgPongByte);
						System.out.println("Pong 전송");
					}
					// Push 메시지 일 경우
					else if (msg.equals(ClientConst.JSON_VALUE_PUSH)) {
						pushData = ServerUtils.parsePushMessage(new JSONParser(), new String(body, ClientConst.CHARSET), pushData);
						System.out.println(pushData.getOrder_list().get(0).getProduct().toString());
					}
				} // end of while
			} catch (IOException e) {
				try {
					// Ping 메시지 전송
					System.out.println("Time out 발생...");
					String msgPingString = ServerUtils.makeJSONMessageForPingPong(new JSONObject(), true);
					byte[] msgPingByte = ServerUtils.makeMessageStringToByte(
							new byte[ClientConst.HEADER_LENTH + msgPingString.getBytes(ClientConst.CHARSET).length], msgPingString);
					bos.write(msgPingByte);
					bos.flush();
					System.out.println("ping 전송");

					try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}

					readCount = bis.read(header);

					// 수신된 메시지 DATASIZE
					headerLength = ServerUtils.byteToInt(header);
					// DATA 길이만큼 byte배열 선언
					byte[] body = new byte[headerLength];
					bodyLength = bis.read(body);
					String msg = ServerUtils.parseJSONMessage(new JSONParser(), new String(body, ClientConst.CHARSET));
					// Pong 메시지 일 경우
					if (msg.equals(ClientConst.JSON_VALUE_PONG)) {
						System.out.println("Pong 도착");
					}
				} catch (IOException e1) {
					// 서버가 죽은 경우
					boolean flag = true;
					while (flag) {
						if (connectServer() && !OIOServer.isSurvival()) {
							processMsg();
							flag = false;
						} else {
							System.out.println("서버 오작동중 ...");
						}
					}

				}
			}
		} // end of while
	} // end of processMsg()

	public static void main(String[] args) {
		OIOClient mcc = new OIOClient();
		mcc.connectServer();
		mcc.processMsg();
	}
}