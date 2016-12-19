package server.service;

import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import server.exception.AlreadyConnectedSocketException;
import server.exception.PushMessageSendingException;
import server.model.OrderInfo;
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
 * @TODO 클라이언트를 관리하기 위한 자료구조(HashMap-> Collection.syncronized()
 *       Wrapping->ConcurrentHashMap) Thread pooling {@link Pushable} 구현
 */
public class SocketConnectionManager implements Pushable {
	// 매니저 객체는 싱글톤
	private static SocketConnectionManager instance = null;

	public static SocketConnectionManager getInstance() {
		if (instance == null) {
			instance = new SocketConnectionManager();
			ServerConst.ACCESS_LOGGER.debug("Create ConnectionManager");
		}
		return instance;
	}

	// 클라이언트 처리 쓰레드를 Map으로 관리
	// private Map<String, ProcessCilentRequest> conMap = new HashMap<String,
	// ProcessCilentRequest>();
	private ConcurrentHashMap<String, ProcessCilentRequest> concurrentHashMap = new ConcurrentHashMap<String, ProcessCilentRequest>();
	// 쓰레드풀 구현부분
	private ExecutorService executorService = Executors.newCachedThreadPool();

	private DBThread dbThread;
	
	public LinkedBlockingQueue<String> unreceivedAckQueue = 
			new LinkedBlockingQueue<String>(ServerConst.RECEIVED_ACK_QUEUE_SIZE);
	private SocketConnectionManager() {
		dbThread = new DBThread(this, unreceivedAckQueue);
		ServerConst.ACCESS_LOGGER.debug("DBThread Created!");
		dbThread.start();
		ServerConst.ACCESS_LOGGER.debug("DBThread Running...");
	}

	@Override
	public void sendPushAll(PushInfo msg) {
		//ConcurrentHashMap의 특성상 순회 도중에 다른 스레드의 접근으로 인헌 ConcurrentModificationException 발생 안된다???
		//그렇다면 메소드단위의 sync 해제해도 무방
		ServerConst.MESSAGE_LOGGER.debug("Push Message Sending Start...");
		for(String key : concurrentHashMap.keySet()){
			try{
				concurrentHashMap.get(key).setPushAll(msg);
			} catch(PushMessageSendingException e){
				concurrentHashMap.remove(key);
				ServerConst.MESSAGE_LOGGER.error("Remove Client [{}] Connection in HashMap", key);
			}
		}
		ServerConst.MESSAGE_LOGGER.debug("Push Message Sending End!");
	}

	@Override
	public void sendPushPartial(OrderInfo msg) {
		try {
			concurrentHashMap.get(msg.getOrder_seller()).setPushPartial(msg);
			ServerConst.MESSAGE_LOGGER.debug("Order Message Send to [{}]", msg.getOrder_seller());
		} catch (PushMessageSendingException e) {
			concurrentHashMap.remove(msg.getOrder_seller());
			ServerConst.MESSAGE_LOGGER.error("Remove Client [{}] Connection in HashMap", msg.getOrder_seller());
		}
	}

	/**
	 * 쓰레드풀과 Map에 추가시키는 메소드 리턴타입으로 Future 객체를 사용할지에 대한 여부 고려
	 * 
	 * @param name
	 *            클라이언트 아이디
	 * @param clientSocket
	 *            클라이언트와 연결된 소켓
	 */
	public void addClientSocket(String name, Socket clientSocket, String aesKey)
											throws AlreadyConnectedSocketException {
		// 중복이 아니라면 쓰레드를 생성하고 Map에 담음
		if (concurrentHashMap.containsKey(name)) {
			// TODO : 인증이 되지 않았다는 메시지를 보내고 소켓을 닫는 것을 던짐
			ServerConst.ACCESS_LOGGER.info("Client [{}] is aleady Connected", name);
			throw new AlreadyConnectedSocketException("Aleady Connected Exception");
		} else {
			ProcessCilentRequest pcr = new ProcessCilentRequest(clientSocket, aesKey, unreceivedAckQueue);
			concurrentHashMap.put(name, pcr);
			this.executorService.submit(pcr);
			ServerConst.ACCESS_LOGGER.info("ProcessClientRequest Thread Start, Client Name : [{}], Connection total Number : [{}]", name,concurrentHashMap.size());
		}
		/*
		 //이렇게 한다면 메소드 단위의 sync 안해줘도 될지도? 
		 try {
			this.executorService.submit(
					concurrentHashMap.putIfAbsent(
							name, new ProcessCilentRequest(clientSocket, aesKey, receivedAckQueue)));
		} catch (NullPointerException e) {
			
		}*/
		
	}

	public synchronized void closeAll() {
		dbThread.interrupt();
		executorService.shutdown();
		concurrentHashMap.clear();
		ServerConst.ACCESS_LOGGER.debug("Manager's Resourcese Closed");
	}

}
