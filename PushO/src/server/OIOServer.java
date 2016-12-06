package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import exception.EmptyResultDataException;
import observer.DBObserver;
import observer.DBThread;
import res.Const;

public class OIOServer implements DBObserver {
/**
 * @author 		�ֺ�ö
 * @Description	���� ���α׷�, OIO����� Socket���, ������ Controller Ŭ����
 * 				�ټ��� Ŭ���̾�Ʈ���� ������ �����ϴ� ������� ArrayList<Socket>�� �����
 * 				Socket���� �� ���� ����� ������ �̱��� �������� ������ {@link AuthClientProxy}�� ���
 * 				������ �������� ��� {@link EmptyResultDataException}�� ���� ���� ����
 * 				������ ���� �Ŀ��� ������ �� Ŭ���̾�Ʈ�� ����ϴ� {@link ProcessCilentRequest}�� ����
 * TODO			��Ƽ�����带 ���� �ټ��� Ŭ���̾�Ʈ ���� ����(Thread pooling)
 * 				{@link AuthClientProxy}�� �̱����̾��� ��� �����߻� ���� ���
 * 				Ŭ���̾�Ʈ�� ������ �Ǿ��� ���� �ʱ�ȭ �۾�(����, ��ȣȭ ��)
 * 				Ÿ�Ӿƿ��� �߻��Ͽ��� ��� �ڿ����� ��Ŀ����
 */
	
	public static void main(String[] args) {
		new OIOServer();
	}
	private ServerSocket serverSocket;
	private Socket socket;
	private DBThread dbThread;
	
	public HashMap<String, ProcessCilentRequest> socketList = new HashMap<String, ProcessCilentRequest>();
	private Iterator<String> keySetIterator;
	
	private String userName;
	
	public OIOServer() {
		try {
			
			serverSocket = new ServerSocket(Const.PORT_NUM);
			System.out.println("��������...");
			
			dbThread = new DBThread(this);
			dbThread.start();
			
			
			//������ ���� ���Ͻ� Ŭ������ �ν��Ͻ� ȹ��
			AuthClientProxy authProxy = AuthClientProxy.getInstance();
			while (true) {
				//���ŷ ����
				socket = serverSocket.accept();
				//��Ʈ���� ���� Ÿ�Ӿƿ� ����
//				socket.setSoTimeout(Const.STREAM_TIME_OUT);
				try {
					//������ ����(DB��ȸ) �� �����Ѵٸ� Ŭ���̾�Ʈ ��ûó�� ������ ����
					System.out.println("������ ���� ����");
					ProcessCilentRequest thread = authProxy.getClientSocketThread(socket, this);
					thread.start();
					//����Ʈ�� ����
					socketList.put(userName, thread);
					System.out.println("Client ����ó�� ������ : " + thread.getId() + " , " + thread.getName());
				} catch (EmptyResultDataException e) {
					// TODO ������ ���� ���� ������� ��� ó�� ����
					e.printStackTrace();
					socket.close();
					//socketList.remove(thread);
					System.out.println("���� ����, ���� ����");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("�������� ���� : " + e.getMessage());
		} finally {
			try {
				socket.close();
				serverSocket.close();
				dbThread.obserberStop();
				socketList = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void msgPush(String msg) {
		System.out.println("Ǫ�������� : " + msg);
		keySetIterator = socketList.keySet().iterator();
		while (keySetIterator.hasNext()) {
		    String userID = keySetIterator.next();
		    socketList.get(userID).setPush(msg); 
		}
	}

	@Override
	public void setUser(String id) {
		userName = id;
	}
}

