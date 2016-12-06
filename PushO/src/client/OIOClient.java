package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import res.Const;
import util.Utils;

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

	// 서버에 연결 작업
	public boolean connectServer() {
		try {
			socket = new Socket(Const.SERVER_IP, Const.PORT_NUM);
			// 입력스트림에 대한 타임아웃 설정
			socket.setSoTimeout(Const.STREAM_TIME_OUT);

			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());
			String msgAuthString = Utils.makeJSONMessageForAuth("다우마트사장", "비밀번호~?", new JSONObject(), new JSONObject());
			byte[] msgAuthByte = Utils.makeMessageStringToByte(
					new byte[Const.HEADER_LENTH + msgAuthString.getBytes().length], msgAuthString);
			bos.write(msgAuthByte);
			bos.flush();
			System.out.println("인증 보냄");
			processMsg();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void processMsg() {
		boolean status = true;
		int readCount = 0;
		int headerLength = 0;
		int bodyLength = 0;
		// 수신된 메시지 DATASIZE
		byte[] header = new byte[Const.HEADER_LENTH];
		/**
		 * 실제 데이터를 주고 받는 부분 TODO : 핑퐁뿐만 아니라 실제 데이터를 주고받는 로직
		 */
		while (status) {
			try {
				// timeout 설정
				socket.setSoTimeout(Const.SEND_WATING_TIME);
				while ((readCount = bis.read(header)) != -1) {

					// 수신된 메시지 DATASIZE
					headerLength = Utils.byteToInt(header);
					// DATA 길이만큼 byte배열 선언
					byte[] body = new byte[headerLength];
					bodyLength = bis.read(body);
					String msg = Utils.parseJSONMessage(new JSONParser(), new String(body));

					// Ping 메시지 일 경우
					if (msg.equals(Const.JSON_VALUE_PING)) {
						String msgPongString = Utils.makeJSONMessageForPingPong(new JSONObject(), false);
						byte[] msgPongByte = Utils.makeMessageStringToByte(
								new byte[Const.HEADER_LENTH + msgPongString.getBytes().length], msgPongString);
						bos.write(msgPongByte);
						System.out.println("Pong 전송");
					}
					// Push 메시지 일 경우
					else if (msg.equals(Const.JSON_VALUE_PUSH)) {
						System.out.println("push 메시지");
					}
				} // end of while

			} catch (IOException e) {
				try {
					// Ping 메시지 전송
					System.out.println("Time out 발생...");
					String msgPingString = Utils.makeJSONMessageForPingPong(new JSONObject(), true);
					byte[] msgPingByte = Utils.makeMessageStringToByte(
							new byte[Const.HEADER_LENTH + msgPingString.getBytes().length], msgPingString);
					bos.write(msgPingByte);
					bos.flush();
					System.out.println("ping 전송");
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

				try {
					// 수신된 메시지 DATASIZE
					headerLength = Utils.byteToInt(header);
					// DATA 길이만큼 byte배열 선언
					byte[] body = new byte[headerLength];
					bodyLength = bis.read(body);
					String msg = Utils.parseJSONMessage(new JSONParser(), new String(body));

					// Pong 메시지 일 경우
					if (msg.equals(Const.JSON_VALUE_PONG)) {
						System.out.println("Pong 도착");
					}
				} catch (IOException e1) {
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

		}

		// 클라이언트가 죽은 경우
		System.out.println("[MultiChatClient]" + "종료됨!");
		status = false;

	}

	public static void main(String[] args) {
		OIOClient mcc = new OIOClient();
		mcc.connectServer();
		mcc.processMsg();
	}
}
