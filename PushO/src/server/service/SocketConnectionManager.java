package server.service;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
 * @author �ֺ�ö
 * @Description {@link OIOServer}������ accept()�� ���� ���ῡ ���� ó������ �ϰ�
 *              {@link AuthClientHandler}���� ������ ��ģ �Ŀ�
 *              {@link ProcessCilentRequest}�� �����Ͽ� �ش� �����带 �����ϱ� ���� ThreadPool��
 *              Ŭ���̾�Ʈ���� �����ϴ� �ڷᱸ���� ���� {@link Pushable}�� �����Ͽ� Ư�� �Ǹ��ڿ��Ը� ������ �޼ҵ��
 *              ��ο��� ������ �޼ҵ带 ����
 * @TODO Ŭ���̾�Ʈ�� �����ϱ� ���� �ڷᱸ��(HashMap-> Collection.syncronized()
 *       Wrapping->ConcurrentHashMap) Thread pooling {@link Pushable} ����
 */
public class SocketConnectionManager implements Pushable {
	// �Ŵ��� ��ü�� �̱���
	private static SocketConnectionManager instance = null;

	public static SocketConnectionManager getInstance() {
		if (instance == null) {
			instance = new SocketConnectionManager();
			ServerConst.ACCESS_LOGGER.debug("Create ConnectionManager");
		}
		return instance;
	}

	// Ŭ���̾�Ʈ ó�� �����带 Map���� ����
	// private Map<String, ProcessCilentRequest> conMap = new HashMap<String,
	// ProcessCilentRequest>();
	private ConcurrentHashMap<String, ProcessCilentRequest> concurrentHashMap =
								new ConcurrentHashMap<String, ProcessCilentRequest>();
	// ������Ǯ �����κ�
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
		// ConcurrentHashMap�� Ư���� ��ȸ ���߿� �ٸ� �������� �������� ����
		// ConcurrentModificationException �߻� �ȵȴ�???
		// �׷��ٸ� �޼ҵ������ sync �����ص� ����
		ServerConst.MESSAGE_LOGGER.debug("Push Message Sending Start...");
		for (String key : concurrentHashMap.keySet()) {
			try {
				concurrentHashMap.get(key).setPushAll(msg);
			} catch (PushMessageSendingException e) {
				concurrentHashMap.remove(key);
				ServerConst.MESSAGE_LOGGER.error("Remove Client [{}] Connection in HashMap", key);
			}
		}
		ServerConst.MESSAGE_LOGGER.debug("Push Message Sending End!");
	}

	@Override
	public void sendPushPartial(List<OrderInfo> orderList) {
		for (OrderInfo orderinfo : orderList) {
			if (concurrentHashMap.containsKey(orderinfo.getOrder_seller())) {
				try {
					concurrentHashMap.get(orderinfo.getOrder_seller()).setPushPartial(orderinfo);
					ServerConst.MESSAGE_LOGGER.debug("Order Message Send to [{}]",
							orderinfo.getOrder_seller());
				} catch (PushMessageSendingException e) {
					concurrentHashMap.remove(orderinfo.getOrder_seller());
					ServerConst.MESSAGE_LOGGER.error("Remove Client [{}] Connection in HashMap",
							orderinfo.getOrder_seller());
				}
			}
		}

	}

	/**
	 * ������Ǯ�� Map�� �߰���Ű�� �޼ҵ� ����Ÿ������ Future ��ü�� ��������� ���� ���� ���
	 * 
	 * @param name
	 *            Ŭ���̾�Ʈ ���̵�
	 * @param clientSocket
	 *            Ŭ���̾�Ʈ�� ����� ����
	 */
	public void addClientSocket(String name, Socket clientSocket, String aesKey)
			throws AlreadyConnectedSocketException {
		// �ߺ��� �ƴ϶�� �����带 �����ϰ� Map�� ����
		if (concurrentHashMap.containsKey(name)) {
			// TODO : ������ ���� �ʾҴٴ� �޽����� ������ ������ �ݴ� ���� ����
			ServerConst.ACCESS_LOGGER.info("Client [{}] is aleady Connected", name);
			throw new AlreadyConnectedSocketException("Aleady Connected Exception");
		} else {
			ProcessCilentRequest pcr = new ProcessCilentRequest(clientSocket, aesKey, unreceivedAckQueue);
			concurrentHashMap.put(name, pcr);
			this.executorService.submit(pcr);
			ServerConst.ACCESS_LOGGER.info(
					"ProcessClientRequest Thread Start, Client Name : [{}], Connection total Number : [{}]", name,
					concurrentHashMap.size());
		}
		/*
		 * //�̷��� �Ѵٸ� �޼ҵ� ������ sync �����൵ ������? try { this.executorService.submit(
		 * concurrentHashMap.putIfAbsent( name, new
		 * ProcessCilentRequest(clientSocket, aesKey, receivedAckQueue))); }
		 * catch (NullPointerException e) {
		 * 
		 * }
		 */

	}

	public synchronized void closeAll() {
		dbThread.interrupt();
		executorService.shutdown();
		concurrentHashMap.clear();
		ServerConst.ACCESS_LOGGER.debug("Manager's Resourcese Closed");
	}

}
