package server.service;

import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
public class SocketConnectionManager extends Thread implements Pushable {
	// �Ŵ��� ��ü�� �̱���
	private static SocketConnectionManager instance = null;

	public static SocketConnectionManager getInstance() {
		if (instance == null) {
			instance = new SocketConnectionManager();
		}
		return instance;
	}

	// Ŭ���̾�Ʈ ó�� �����带 Map���� ����
	private Map<String, ProcessCilentRequest> conMap = new HashMap<String, ProcessCilentRequest>();
	// ������Ǯ �����κ�
	private ExecutorService executorService = Executors.newCachedThreadPool();

	private DBThread dbThread;
	// �ӽú���
	private ProcessCilentRequest proClient = null;

	private Iterator<String> keySetIterator;

	public SocketConnectionManager() {
		dbThread = new DBThread(this);
		dbThread.start();
		System.out.println("DB���� ����...");
	}

	@Override
	public void sendPushAll(String msg) {
		keySetIterator = conMap.keySet().iterator();
		while (keySetIterator.hasNext()) {
			String userID = keySetIterator.next();
			// thread�� setPush
			conMap.get(userID).setPush(msg);
		}
	}

	@Override
	public void sendPushPartial(String Id, String msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			while (!currentThread().isInterrupted()) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * ������Ǯ�� Map�� �߰���Ű�� �޼ҵ� ����Ÿ������ Future ��ü�� ��������� ���� ���� ���
	 * 
	 * @param name
	 *            Ŭ���̾�Ʈ ���̵�
	 * @param clientSocket
	 *            Ŭ���̾�Ʈ�� ����� ����
	 * @param autorized
	 *            �����Ǿ����� ����
	 */
	public synchronized void add(String name, Socket clientSocket, boolean autorized) {
		if (conMap.containsKey(name)) {
			autorized = false;
		}
		if (autorized) {
			proClient = new ProcessCilentRequest(clientSocket);
			this.executorService.submit(proClient);
			conMap.put(name, proClient);
			proClient = null;
		} else {
			// TODO : ������ ���� �ʾҴٴ� �޽����� ������ ������ ����.
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
		this.interrupt();
		conMap = null;
	}

}
