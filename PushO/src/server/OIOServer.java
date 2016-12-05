package server;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import exception.EmptyResultDataException;
import res.Const;

/**
 * @author 		최병철
 * @Description	서버 프로그램, OIO방식의 Socket통신, 일종의 Controller 클래스
 * 				다수의 클라이언트와의 연결을 관리하는 방법으로 ArrayList<Socket>을 사용중
 * 				Socket연결 시 최초 사용자 인증은 싱글톤 패턴으로 구현된 {@link AuthClientProxy}를 사용
 * 				인증이 실패했을 경우 {@link EmptyResultDataException}를 통해 연결 해제
 * 				인증이 끝난 후에는 실제로 각 클라이언트와 통신하는 {@link ProcessCilentRequest}를 실행
 * TODO			멀티쓰레드를 통한 다수의 클라이언트 소켓 관리(Thread pooling)
 * 				{@link AuthClientProxy}가 싱글톤이었을 경우 문제발생 여부 고려
 * 				클라이언트와 연결이 되었을 때의 초기화 작업(인증, 암호화 등)
 * 				타임아웃이 발생하였을 경우 자원관리 매커니즘
 */
public class OIOServer {
	
	public static void main(String[] args) {
		new OIOServer();
	}
	private ServerSocket serverSocket;
	private Socket socket;
	
	ArrayList<Socket> socketList = new ArrayList<Socket>();

	public OIOServer() {
		try {
			
			serverSocket = new ServerSocket(Const.PORT_NUM);
			System.out.println("서버시작...");
			
			//인증을 위한 프록시 클래스의 인스턴스 획득
			AuthClientProxy authProxy = AuthClientProxy.getInstance();
			while (true) {
				//블로킹 구간
				socket = serverSocket.accept();
				//스트림에 대한 타임아웃 설정
				socket.setSoTimeout(Const.STREAM_TIME_OUT);
				try {
					//인증을 실행(DB조회) 후 성공한다면 클라이언트 요청처리 쓰레드 시작
					ProcessCilentRequest thread = authProxy.getClientSocketThread(socket);
					thread.start();
					//리스트로 관리
					socketList.add(socket);
					System.out.println("Client 연결처리 스레드 : " + thread.getId() + " , " + thread.getName());
				} catch (EmptyResultDataException e) {
					// TODO 인증이 되지 않은 사용자일 경우 처리 로직
					e.printStackTrace();
					socket.close();
					socketList.remove(socket);
					System.out.println("인증 실패, 소켓 닫힘");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("서버소켓 예외 : " + e.getMessage());
		} finally {
			try {
				socket.close();
				serverSocket.close();
				socketList = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
