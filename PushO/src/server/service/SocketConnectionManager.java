package server.service;

import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import server.exception.AlreadyConnectedSocketException;
import server.exception.PushMessageSendingException;
import server.model.PushInfo;
import server.observer.DBThread;
import server.res.ServerConst;

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
			ServerConst.SERVER_LOGGER.debug("매니저 생성");
		}
		return instance;
	}

	// 클라이언트 처리 쓰레드를 Map으로 관리
//	private Map<String, ProcessCilentRequest> conMap = new HashMap<String, ProcessCilentRequest>();
	private ConcurrentHashMap<String, ProcessCilentRequest> concurrentHashMap = 
									new ConcurrentHashMap<String, ProcessCilentRequest>();
	// 쓰레드풀 구현부분
	private ExecutorService executorService = Executors.newCachedThreadPool();
	
	private DBThread dbThread;
	
	public LinkedBlockingQueue<String> receivedAckQueue = 
			new LinkedBlockingQueue<String>(ServerConst.RECEIVED_ACK_QUEUE_SIZE);
	
	private SocketConnectionManager() {
		dbThread = new DBThread(this, receivedAckQueue);
		ServerConst.SERVER_LOGGER.debug("DB쓰레드 생성");
		dbThread.start();
		ServerConst.SERVER_LOGGER.debug("DB쓰레드 실행");
	}

	@Override
	public synchronized void sendPushAll(String msg) {
		Iterator<String> keySetIterator = concurrentHashMap.keySet().iterator();
		// 몇명의 사용자에게 보낸것인지 명시하기위해 추가
		int size = 0;
		int sizeTotal = 0;
		// 총 사용자 체크
		while (keySetIterator.hasNext()) {
			sizeTotal++;
		}
		
		ServerConst.SERVER_LOGGER.debug("모든 사용자에게 Push메시지 전송 시작");
		while (keySetIterator.hasNext()) {
			size++;
			String userID = keySetIterator.next();
			ServerConst.SERVER_LOGGER.debug("모든 사용자에게 전송중" + "(" + size + "/" + sizeTotal + ")");
			sendPushPartial(userID,msg);
		}
		ServerConst.SERVER_LOGGER.debug("모든 사용자에게 Push메시지 전송 끝");
	}
	
	@Override
	public void sendPushPartial(String Id, String msg) {
		ServerConst.SERVER_LOGGER.debug( Id+", 사용자에게 Push메시지 전송 시작");
		try {
			concurrentHashMap.get(Id).setPush(msg);
		} catch (PushMessageSendingException e) {
			concurrentHashMap.remove(Id);
			e.printStackTrace();
			ServerConst.SERVER_LOGGER.error(e.getMessage()+", 사용자 "+Id+"를 맵에서 제거");
		}
		ServerConst.SERVER_LOGGER.debug( Id+", 사용자에게 Push메시지 전송 끝");
	}

	/**
	 * 쓰레드풀과 Map에 추가시키는 메소드 리턴타입으로 Future 객체를 사용할지에 대한 여부 고려
	 * 
	 * @param name
	 *            클라이언트 아이디
	 * @param clientSocket
	 *            클라이언트와 연결된 소켓
	 */
	public synchronized void addClientSocket(String name, Socket clientSocket, String aesKey)
			throws AlreadyConnectedSocketException{

		boolean duplicated = false;
		//이미 등록된 사용자인지 검사
		if (concurrentHashMap.containsKey(name)) {
			duplicated = true;
			ServerConst.SERVER_LOGGER.debug(name+"은 이미 맵에 등록됨");
		}
		//중복이 아니라면 쓰레드를 생성하고 Map에 담음
		if (!duplicated) {
			ProcessCilentRequest proClient = new ProcessCilentRequest(clientSocket, aesKey, receivedAckQueue);
			this.executorService.submit(proClient);
			ServerConst.SERVER_LOGGER.info(proClient+"쓰레드 시작, 클라이언트 이름 : "+name);
			concurrentHashMap.put(name, proClient);
			ServerConst.SERVER_LOGGER.info("연결된 클라이언트 수 : "+concurrentHashMap.size());
		} else {
			// TODO : 인증이 되지 않았다는 메시지를 보내고 소켓을 닫는 것을 던짐
			throw new AlreadyConnectedSocketException(name+"은 이미 존재");
		}
	}

	public synchronized void closeAll() {
		ServerConst.SERVER_LOGGER.debug("매니저의 자원해제 시작");
		dbThread.interrupt();
		executorService.shutdown();
		concurrentHashMap.clear();
		ServerConst.SERVER_LOGGER.debug("매니저의 자원해제 끝");
	}


}
