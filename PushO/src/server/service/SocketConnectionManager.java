package server.service;

import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import server.exception.AlreadyConnectedSocketException;
import server.exception.PushMessageSendingException;
import server.observer.DBThread;

/**
 * @author �ֺ�ö
 * @Description {@link OIOServer}������ accept()�� ���� ���ῡ ���� ó������ �ϰ�
 *              {@link AuthClientHandler}���� ������ ��ģ �Ŀ�
 *              {@link ProcessCilentRequest}�� �����Ͽ� �ش� �����带 �����ϱ� ���� ThreadPool��
 *              Ŭ���̾�Ʈ���� �����ϴ� �ڷᱸ���� ���� {@link Pushable}�� �����Ͽ� Ư�� �Ǹ��ڿ��Ը� ������ �޼ҵ��
 *              ��ο��� ������ �޼ҵ带 ���� 
 * @TODO Ŭ���̾�Ʈ�� �����ϱ� ���� �ڷᱸ��(HashMap->
 *      				Collection.syncronized() Wrapping->ConcurrentHashMap) 
 *      Thread pooling {@link Pushable} ����
 */
public class SocketConnectionManager implements Pushable {
	// �Ŵ��� ��ü�� �̱���
	private static SocketConnectionManager instance = null;

	public static SocketConnectionManager getInstance() {
		if (instance == null) {
			instance = new SocketConnectionManager();
		}
		return instance;
	}

	// Ŭ���̾�Ʈ ó�� �����带 Map���� ����
//	private Map<String, ProcessCilentRequest> conMap = new HashMap<String, ProcessCilentRequest>();
	private ConcurrentHashMap<String, ProcessCilentRequest> concurrentHashMap = 
									new ConcurrentHashMap<String, ProcessCilentRequest>();
	// ������Ǯ �����κ�
	private ExecutorService executorService = Executors.newCachedThreadPool();
	
	private DBThread dbThread;

	private SocketConnectionManager() {
		dbThread = new DBThread(this);
		dbThread.start();
		System.out.println("DB���� ����...");
	}

	@Override
	public synchronized void sendPushAll(String msg) {
		Iterator<String> keySetIterator = concurrentHashMap.keySet().iterator();
		while (keySetIterator.hasNext()) {
			String userID = keySetIterator.next();
			sendPushPartial(userID,msg);
		}
	}

	public synchronized void addClientSocket(String name, Socket clientSocket, String aesKey) {

		boolean duplicated = false;
		//�̹� ��ϵ� ��������� �˻�
		if (concurrentHashMap.containsKey(name)) {
			duplicated = true;
		}
		//�ߺ��� �ƴ϶�� �����带 �����ϰ� Map�� ����
		if (!duplicated) {
			ProcessCilentRequest proClient = new ProcessCilentRequest(clientSocket, aesKey);
			this.executorService.submit(proClient);
			System.out.println("���� ���� Ŭ���̾�Ʈ�� �̸� : "+name);
			concurrentHashMap.put(name, proClient);
			System.out.println("����� Ŭ���̾�Ʈ �� : "+concurrentHashMap.size());
		} else {
			// TODO : ������ ���� �ʾҴٴ� �޽����� ������ ������ �ݴ� ���� ����
			throw new AlreadyConnectedSocketException(name+"�� �̹� ����");
		}
	}

	public synchronized void closeAll() {
		dbThread.interrupt();
		executorService.shutdown();
		concurrentHashMap.clear();
	}

	@Override
	public void sendPushPartial(String Id, String msg) {
		// TODO Auto-generated method stub
		
	}

}
