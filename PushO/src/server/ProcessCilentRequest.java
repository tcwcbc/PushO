package server;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import dao.JDBCTemplate;
import exception.EmptyResultDataException;
import res.Const;
import util.Utils;

/**
 * @author		최병철
 * @Description	실제로 각 클라이언트와 소켓통신을 하며 작업을 수행하는 쓰레드
 * TODO			입출력을 위한 스트림을 문자스트림->바이트스트림 으로 변환
 * 				수정된 유틸클래스 사용으로 메시지 작성방식 변경
 * 				타임아웃 예외가 발생했을 경우 알림메시지를 전송하고 소켓해제 및 자원회수 매커니즘
 * 				예외처리들을 위한 많은 try-catch문을 정리
 */
public class ProcessCilentRequest extends Thread {

	private Socket connectedSocketWithClient;
	private BufferedReader br;
	private BufferedWriter bw;
	
	public ProcessCilentRequest(Socket socket) {
		this.connectedSocketWithClient = socket;
	}
	
	@Override
	public void run(){
		try {
			br = new BufferedReader(new InputStreamReader(
					connectedSocketWithClient.getInputStream()));
			bw = new BufferedWriter(new OutputStreamWriter(
					connectedSocketWithClient.getOutputStream()));
			
			bw.write(Utils.makeJSONMessageForPingPong(true));
			System.out.println("시작을 위한 Ping 전송");
			bw.flush();
			
			//핑퐁릴레이
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
		}catch (SocketTimeoutException timeoutE){
			//TODO 타임아웃 발생 시에 자원회수 및 처리 매커니즘
			timeoutE.printStackTrace();
			try {
				bw.write(Utils.makeJSONMessageForPingPong(true));
				System.out.println("Ping 전송");
				
			} catch (IOException e) {
				e.printStackTrace();
				try {
					br.close();
					bw.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
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
