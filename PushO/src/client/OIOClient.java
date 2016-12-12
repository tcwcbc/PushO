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

	private int readCount;
	private int dataSize;
	private int bodyLength;

	// 서버에 연결 작업
	public boolean connectServer() {
		boolean isServerSurvival = false;
		try {
			socket = new Socket(ClientConst.SERVER_IP, ClientConst.PORT_NUM);
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());

			isServerSurvival = true;
			// 인증을 위한 JSON 메세지 생성
			String msgAuthString = ClientUtils.makeJSONMessageForAuth("판매자5", "비밀번호~?", new JSONObject(),
					new JSONObject());
			byte[] msgAuthByte = ClientUtils.makeMessageStringToByte(
					new byte[ClientConst.HEADER_LENTH + msgAuthString.getBytes(ClientConst.CHARSET).length],
					msgAuthString);
			bos.write(msgAuthByte);
			bos.flush();
			System.out.println("인증 메시지 전송");

			// 입력스트림에 대한 7초 타임아웃 설정
			socket.setSoTimeout(ClientConst.SEND_WATING_TIME);
			// 메시지 DATASIZE
			byte[] header = new byte[ClientConst.HEADER_LENTH];
			while ((readCount = bis.read(header)) != -1) {
				// 수신된 메시지 DATASIZE
				dataSize = ClientUtils.byteToInt(header);
				// DATA 길이만큼 byte배열 선언
				byte[] body = new byte[dataSize];
				bodyLength = bis.read(body);
				String msg = ClientUtils.parseJSONMessage(new JSONParser(), new String(body, ClientConst.CHARSET));
				// Pong 메시지 일 경우
				if (msg.equals(ClientConst.JSON_VALUE_PONG)) {
					System.out.println("인증 성공");
					return true;
				}
			} // end of while
			return false;
		} catch (IOException e) {
			if (isServerSurvival == false) {
				System.out.println("Server Connection Exception 발생!!");
				return false;
			} else {
				System.out.println("No Server Response 발생!!");
				return false;
			}
		}
	}

	// 메시지 송수신 메소드
	public void processMsg() {
		boolean status = true;

		// 메시지 DATASIZE
		byte[] header = new byte[ClientConst.HEADER_LENTH];
		// 실제 데이터를 주고 받는 부분 TODO : 핑퐁뿐만 아니라 실제 데이터를 주고받는 로직
		while (status) {
			try {
				// 입력스트림에 대한 7초 타임아웃 설정
				socket.setSoTimeout(ClientConst.SEND_WATING_TIME);
				while ((readCount = bis.read(header)) != -1) {
					// 수신된 메시지 DATASIZE
					dataSize = ClientUtils.byteToInt(header);
					// DATA 길이만큼 byte배열 선언
					byte[] body = new byte[dataSize];
					bodyLength = bis.read(body);
					String msg = ClientUtils.parseJSONMessage(new JSONParser(), new String(body, ClientConst.CHARSET));

					// Ping 메시지 일 경우
					if (msg.equals(ClientConst.JSON_VALUE_PING)) {
						String msgPongString = ClientUtils.makeJSONMessageForPingPong(new JSONObject(), false);
						byte[] msgPongByte = ClientUtils.makeMessageStringToByte(
								new byte[ClientConst.HEADER_LENTH + msgPongString.getBytes(ClientConst.CHARSET).length],
								msgPongString);
						bos.write(msgPongByte);
						System.out.println("pong 전송");
					}
					// Push 메시지 일 경우
					else if (msg.equals(ClientConst.JSON_VALUE_PUSH)) {
						pushData = ClientUtils.parsePushMessage(new JSONParser(), new String(body, ClientConst.CHARSET),
								pushData);
						System.out.println(pushData.getOrder_list().get(0).getProduct().toString());
					}
				} // end of while
			} catch (IOException e) {
				try {
					System.out.println("Time out 발생...");
					String msgPingString = ClientUtils.makeJSONMessageForPingPong(new JSONObject(), true);
					byte[] msgPingByte = ClientUtils.makeMessageStringToByte(
							new byte[ClientConst.HEADER_LENTH + msgPingString.getBytes(ClientConst.CHARSET).length],
							msgPingString);
					bos.write(msgPingByte);
					bos.flush();
					System.out.println("ping 전송");

					// 입력스트림에 대한 7초 타임아웃 설정
					socket.setSoTimeout(ClientConst.SEND_WATING_TIME);
					while ((readCount = bis.read(header)) != -1) {
						// 수신된 메시지 DATASIZE
						dataSize = ClientUtils.byteToInt(header);
						// DATA 길이만큼 byte배열 선언
						byte[] body = new byte[dataSize];
						bodyLength = bis.read(body);
						String msg = ClientUtils.parseJSONMessage(new JSONParser(),
								new String(body, ClientConst.CHARSET));
						// Pong 메시지 일 경우
						if (msg.equals(ClientConst.JSON_VALUE_PONG)) {
							System.out.println("pong 도착");
						}
					} // end of while

				} catch (IOException e1) {
					System.out.println("Time out 발생...");
					// 서버가 죽은 경우
					boolean flag = true;
					while (flag) {
						if (connectServer()) {
							processMsg();
							flag = false;
						}
					}
				}
			}
		} // end of while
	} // end of processMsg()

	public static void main(String[] args) {
		OIOClient mcc = new OIOClient();
		boolean flag = true;
		while (flag) {
			if (mcc.connectServer()) {
				mcc.processMsg();
				flag = false;
			}
		}
	}
}