package server.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import server.res.ServerConst;
import server.util.ServerUtils;

/**
 * @author 최병철
 * @Description 실제로 각 클라이언트와 소켓통신을 하며 작업을 수행하는 쓰레드
 * @TODO 입출력을 위한 스트림을 문자스트림->바이트스트림 으로 변환 수정된 유틸클래스 사용으로 메시지 작성방식 변경 타임아웃 예외가
 *       발생했을 경우 알림메시지를 전송하고 소켓해제 및 자원회수 매커니즘 예외처리들을 위한 많은 try-catch문을 정리
 */
public class ProcessCilentRequest extends Thread {

	private Socket connectedSocketWithClient;
	private byte[] msgPingByte;
	private byte[] msgPongByte;
	private byte[] msgPushByte;

	private BufferedOutputStream bos;
	private BufferedInputStream bis;

	public ProcessCilentRequest(Socket socket) {
		this.connectedSocketWithClient = socket;
	}

	@Override
	public void run() {
		try {
			bos = new BufferedOutputStream(connectedSocketWithClient.getOutputStream());
			bis = new BufferedInputStream(connectedSocketWithClient.getInputStream());
			
			/**
			 * 실제 데이터를 주고 받는 부분 
			 * @TODO : 핑퐁뿐만 아니라 실제 데이터를 주고받는 로직
			 */
			startPingPong();
		} catch (SocketTimeoutException timeoutE) {
			// TODO 타임아웃 발생 시에 자원회수 및 처리 매커니즘
			timeoutE.printStackTrace();
			try {
				bos.write(msgPingByte);
				System.out.println("Ping 전송");

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

	private void startPingPong() throws IOException, InterruptedException {
		String msgPingString = ServerUtils.makeJSONMessageForPingPong(new JSONObject(), true);
		msgPingByte = ServerUtils.makeMessageStringToByte(
				new byte[ServerConst.HEADER_LENTH + msgPingString.getBytes().length], msgPingString);
		String msgPongString = ServerUtils.makeJSONMessageForPingPong(new JSONObject(), false);
		msgPongByte = ServerUtils.makeMessageStringToByte(
				new byte[ServerConst.HEADER_LENTH + msgPongString.getBytes().length], msgPongString);

		byte[] buf = new byte[ServerConst.HEADER_LENTH];
		byte[] body;
		int readCount = 0;
		int length = 0;
		int bodylength = 0;

		bos.write(msgPingByte);
		bos.flush();
		System.out.println("시작을 위한 Ping 전송");

		while ((readCount = bis.read(buf)) != -1) {
			length = ServerUtils.byteToInt(buf);
			body = new byte[length];
			bodylength = bis.read(body);
			String pp = ServerUtils.parseJSONMessage(new JSONParser(), new String(body));
			if (pp.equals(ServerConst.JSON_VALUE_PING)) {
				bos.write(msgPongByte);
				System.out.println("Pong 전송");
			}
			if (pp.equals(ServerConst.JSON_VALUE_PONG)) {
				bos.write(msgPingByte);
				System.out.println("Ping 전송");
			}
			bos.flush();
		}
	}

	/**
	 * 클라이언트에게 알림을 전송하는 메소드 String 데이터를 구분자로 split하여 데이터 형식에 맞게 가공한다.
	 * 
	 * @param msg
	 *            주문정보
	 */
	public void setPush(String msg) {
		try {
			msgPushByte = ServerUtils.makeMessageStringToByte(
					new byte[ServerConst.HEADER_LENTH + msg.getBytes(ServerConst.CHARSET).length], msg);

			bos.write(msgPushByte);
			bos.flush();
			System.out.println("푸쉬완료:" + this.getName());
		} catch (IOException e) {
			// 상대 클라이언트 접속이 끊어지면 발생
			// 그에 따라 HashMap에 저장되어있는 현재 Thread를 지우는 작업이 필요함
			System.out.println("setPush() 푸쉬발송중 오류" + e.getMessage());
		}
	}
}
