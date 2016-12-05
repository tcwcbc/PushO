package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

import res.Const;
import util.Utils;

/**
 * @author 		최병철
 * @Description	클라이언트 프로그램, 생성과 동시에 서버소켓으로 접속 후 시나리오에 따라 통신
 * TODO			송수신 하는 스트림을 문자스트림에서 바이트스트림으로 변환
 * 				타임아웃이나 기타 연결문제 복구 매커니즘 구현 및 기타 예외처리
 * 				이벤트를 발생시키거나 데이터를 수신 후 가시적으로 보여줄 View
 */
public class OIOClient {
	
	public static void main(String[] args) {
		new OIOClient();
	}
	
	private BufferedWriter bw;
	private Socket socket;
	private BufferedReader br;
	
	public OIOClient() {
		try {
			socket = new Socket(Const.SERVER_IP, Const.PORT_NUM);
			//입력스트림에 대한 타임아웃 설정
			socket.setSoTimeout(Const.STREAM_TIME_OUT);
			bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			/*
			 * 최초 인증을 위한 메시지를 서버로 송신하는 부분
			 * TODO : 암호화 키 교환, 인증키 수신, 버퍼크기 교환 등의 초기화 작업
			 */
			bw.write(Utils.makeJSONMessageForAuth("가나다", "1111"));
			System.out.println("인증 보냄");
			bw.flush();
			
			/**
			 * 실제 데이터를 주고 받는 부분
			 * TODO : 핑퐁뿐만 아니라 실제 데이터를 주고받는 로직
			 */
			String text = null;
			while((text = br.readLine())!=null){
				Thread.sleep(Const.SEND_WATING_TIME);
				String pp = Utils.parseJSONMessage(text);
				if(pp.equals(Const.JSON_VALUE_PING)){
					bw.write(Utils.makeJSONMessageForPingPong(false));
					System.out.println("Pong 전송");
				}
				if(pp.equals(Const.JSON_VALUE_PONG)){
					bw.write(Utils.makeJSONMessageForPingPong(true));
					System.out.println("Ping 전송");
				}
				bw.flush();
			}
			
			
//			autoPingPong(3);
//			text = br.readLine();
//			autoPingPong(3);
			
		}catch (SocketTimeoutException timeoutE){
			timeoutE.printStackTrace();
			//TODO 타임아웃이 발생했을 경우 복구 매커니즘
			try {
				bw.write(Utils.makeJSONMessageForPingPong(true));
				System.out.println("Ping 전송");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					br.close();
					bw.close();
					socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 테스트를 위한 PingPong 메시지 송수신 메소드
	 * 
	 * @param time					메시지를 수신하고 응답을 보낼 횟수
	 * @throws IOException			
	 * @throws InterruptedException
	 */
	private void autoPingPong(int time) throws IOException, InterruptedException {
		String text;
		for(int i = 0; i<time; i++){
			text = br.readLine();
			Thread.sleep(Const.SEND_WATING_TIME);
			String pp = Utils.parseJSONMessage(text);
			if(pp.equals(Const.JSON_VALUE_PING)){
				bw.write(Utils.makeJSONMessageForPingPong(false));
				System.out.println("Pong 전송");
			}
			if(pp.equals(Const.JSON_VALUE_PONG)){
				bw.write(Utils.makeJSONMessageForPingPong(true));
				System.out.println("Ping 전송");
			}
			bw.flush();
		}
	}
}
