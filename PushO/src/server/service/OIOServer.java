package server.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

import server.exception.EmptyResultDataException;
import server.res.ServerConst;

/**
 * @author 최병철
 * @Description 서버 프로그램, OIO방식의 Socket통신, 일종의 Controller 클래스 다수의 클라이언트와의 연결을
 *              관리하는 방법으로 ArrayList<Socket>을 사용중 Socket연결 시 최초 사용자 인증은 싱글톤 패턴으로
 *              구현된 {@link AuthClientHandler}를 사용 인증이 실패했을 경우
 *              {@link EmptyResultDataException}를 통해 연결 해제 인증이 끝난 후에는 실제로 각
 *              클라이언트와 통신하는 {@link ProcessCilentRequest}를 실행
 * @TODO 멀티쓰레드를 통한 다수의 클라이언트 소켓 관리(Thread pooling) {@link AuthClientHandler}가
 *       싱글톤이었을 경우 문제발생 여부 고려 클라이언트와 연결이 되었을 때의 초기화 작업(인증, 암호화 등) 타임아웃이 발생하였을 경우
 *       자원관리 매커니즘
 */
public class OIOServer {

	public static void main(String[] args) {
		new OIOServer();
	}
	
	public ArrayBlockingQueue<Socket> socketQueue = 
			new ArrayBlockingQueue<Socket>(ServerConst.SOCKET_QUEUE_SIZE);
	// 소켓 및 해당 쓰레드들을 관리하는 매니저클래스 인스턴스 획득
	private SocketConnectionManager conManagerager = SocketConnectionManager.getInstance();
	// 인증을 위한 프록시 클래스의 인스턴스 획득
//	private AuthClientHandler authHandler = AuthClientHandler.getInstance();

	private ServerSocket serverSocket;
	private Socket socket;
	

	public OIOServer() {
		try {
			serverSocket = new ServerSocket(ServerConst.PORT_NUM);
			ServerConst.SERVER_LOGGER.debug("서버시작");

			new AuthClientHandler(socketQueue).start();
			ServerConst.SERVER_LOGGER.debug("핸들러시작");
			
			while (true) {
				ServerConst.SERVER_LOGGER.debug("클라이언트 접속요청 대기");
				// 블로킹 구간
				socket = serverSocket.accept();
				ServerConst.SERVER_LOGGER.debug("클라이언트 접속완료");
				try {
					this.socketQueue.put(socket);
					ServerConst.SERVER_LOGGER.info("블로킹큐에 넣음, 큐 크기 : "+this.socketQueue.size());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					ServerConst.SERVER_LOGGER.error(e.getMessage());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ServerConst.SERVER_LOGGER.error(e.getMessage());
		} finally {
			try {
				
				this.socketQueue.clear();
				socket.close();
				serverSocket.close();
				conManagerager.closeAll();
				ServerConst.SERVER_LOGGER.debug("모든자원 해제");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ServerConst.SERVER_LOGGER.error(e.getMessage());
			}
		}
	}
}
