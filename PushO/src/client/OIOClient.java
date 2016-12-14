package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;

import client.res.ClientConst;

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

	private Timer timer;
	private ClientHeartBeat heartBeat;
	public String desKey;
	final int timeInterval = 10000;

	// 서버에 연결 작업
	public boolean connectServer() {
		boolean isServerSurvival = false;
		try {
			if (socket != null && socket.isConnected()) {
				close();
				System.out.println("Server RE-Connection 시도");
			}

			socket = new Socket(ClientConst.SERVER_IP, ClientConst.PORT_NUM);
			System.out.println("Socket 정보: " + socket);
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());

			// 키교환이 이뤄지는 작업
			// KeyExchangeClient key = new KeyExchangeClient(bis, bos);
			// desKey = key.start();
			// System.out.println("키 교환작업 완료:" + desKey);

			isServerSurvival = true;

			// 인증을 위한 JSON 메세지 생성
			CilentDataProcess.sendAuth(bos);
			System.out.println("인증 메시지 전송");
			CilentDataProcess.receive(socket, bis, bos);

			return true;
		} catch (IOException e) {
			if (isServerSurvival == false) {
				System.out.println("Server Connection Exception 발생!!");
				return false;
			} else {
				System.out.println("No Server Response 발생!!");
				try {
					// 인증을 위한 JSON 메세지 생성
					CilentDataProcess.sendAuth(bos);
					System.out.println("인증 메시지 다시 전송");
					CilentDataProcess.receive(socket, bis, bos);
				} catch (IOException e1) {
					System.out.println("No Server Response 발생!!");
					return false;
				}
				return true;
			}
		}
	}

	// 메시지 송수신 메소드
	public void processMsg() throws IOException {
		boolean status = true;

		timer = new Timer();
		heartBeat = new ClientHeartBeat(bos);
		timer.scheduleAtFixedRate(heartBeat, timeInterval, timeInterval);

		while (status) {
			try {
				CilentDataProcess.receive(socket, bis, bos);
			} catch (IOException e) {
				CilentDataProcess.occurTimeout(socket, bis, bos);
			}
		}
	}

	public void close() {
		try {
			heartBeat = null;
			timer.cancel();
			bis.close();
			bos.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		OIOClient mcc = new OIOClient();
		boolean flag = true;
		while (flag) {
			if (mcc.connectServer()) {
				try {
					mcc.processMsg();
				} catch (IOException e) {
					System.out.println("Time out 발생...");
					continue;
				}
			}
		}
	}
}