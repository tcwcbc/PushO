package server.service;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import server.model.UserAuth;
import server.observer.DBThread;

/**
 * @author 최병철
 * @Description {@link OIOServer}에서는 accept()를 통해 연결에 대한 처리만을 하고
 *              {@link AuthClientHandler}에서 인증을 거친 후에
 *              {@link ProcessCilentRequest}를 생성하여 해당 쓰레드를 관리하기 위한 ThreadPool과
 *              클라이언트들을 관리하는 자료구조가 포함 {@link Pushable}을 구현하여 특정 판매자에게만 보내는 메소드와
 *              모두에게 보내는 메소드를 구현 
 * @TODO 클라이언트를 관리하기 위한 자료구조(HashMap->
 *      				Collection.syncronized() Wrapping->ConcurrentHashMap) 
 *      Thread pooling {@link Pushable} 구현
 */
public class SocketConnectionManager implements Pushable {
	// 매니저 객체는 싱글톤
	private static SocketConnectionManager instance = null;

	public static SocketConnectionManager getInstance() {
		if (instance == null) {
			instance = new SocketConnectionManager();
		}
		return instance;
	}

	// 클라이언트 처리 쓰레드를 Map으로 관리
	private Map<String, ProcessCilentRequest> conMap = new HashMap<String, ProcessCilentRequest>();
	// 쓰레드풀 구현부분
	private ExecutorService executorService = Executors.newCachedThreadPool();

	Iterator<String> keySetIterator;
	
	private DBThread dbThread;

	public SocketConnectionManager() {
		dbThread = new DBThread(this);
		dbThread.start();
		System.out.println("DB감시 시작...");
	}

	@Override
	public void sendPushAll(String msg) {
		keySetIterator = conMap.keySet().iterator();
		while (keySetIterator.hasNext()) {
			String userID = keySetIterator.next();
			// thread의 setPush
			conMap.get(userID).setPush(msg);
		}
	}

	@Override
	public void sendPushPartial(String id, String msg) {
		
	}
/*
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			while (!this.isInterrupted()) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/
	/**
	 * 쓰레드풀과 Map에 추가시키는 메소드 리턴타입으로 Future 객체를 사용할지에 대한 여부 고려
	 * 
	 * @param name
	 *            클라이언트 아이디
	 * @param clientSocket
	 *            클라이언트와 연결된 소켓
	 */
	public synchronized void add(String name, Socket clientSocket, String aesKey) {
		boolean duplicated = false;
		//이미 등록된 사용자인지 검사
		if (conMap.containsKey(name)) {
			duplicated = true;
		}
		if (!duplicated) {
			ProcessCilentRequest proClient = new ProcessCilentRequest(clientSocket, aesKey);
			this.executorService.submit(proClient);
			System.out.println("실행 중인 클라이언트의 이름 : "+name);
			conMap.put(name, proClient);
			System.out.println("연결된 클라이언트 수 : "+conMap.size());
		} else {
			// TODO : 인증이 되지 않았다는 메시지를 보내고 소켓을 닫음.
			System.out.println("이미 존재하는 사용자");
			try {
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public synchronized void closeAll() {
		dbThread.interrupt();
		try {
			dbThread.db.closeDBSet();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		executorService.shutdown();
//		this.interrupt();
		conMap = null;
	}

}
