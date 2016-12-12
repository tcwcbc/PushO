package server.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;

import server.exception.EmptyResultDataException;
import server.observer.DBObserver;
import server.observer.DBThread;
import server.res.ServerConst;

/**
 * @author �ֺ�ö
 * @Description ���� ���α׷�, OIO����� Socket���, ������ Controller Ŭ���� �ټ��� Ŭ���̾�Ʈ���� ������
 *              �����ϴ� ������� ArrayList<Socket>�� ����� Socket���� �� ���� ����� ������ �̱��� ��������
 *              ������ {@link AuthClientHandler}�� ��� ������ �������� ���
 *              {@link EmptyResultDataException}�� ���� ���� ���� ������ ���� �Ŀ��� ������ ��
 *              Ŭ���̾�Ʈ�� ����ϴ� {@link ProcessCilentRequest}�� ����
 * @TODO ��Ƽ�����带 ���� �ټ��� Ŭ���̾�Ʈ ���� ����(Thread pooling) {@link AuthClientHandler}��
 *       �̱����̾��� ��� �����߻� ���� ��� Ŭ���̾�Ʈ�� ������ �Ǿ��� ���� �ʱ�ȭ �۾�(����, ��ȣȭ ��) Ÿ�Ӿƿ��� �߻��Ͽ��� ���
 *       �ڿ����� ��Ŀ����
 */
public class OIOServer {

	public static void main(String[] args) {
		new OIOServer();
	}

	// ���� �� �ش� ��������� �����ϴ� �Ŵ���Ŭ���� �ν��Ͻ� ȹ��
	private SocketConnectionManager conManagerager = SocketConnectionManager.getInstance();
	// ������ ���� ���Ͻ� Ŭ������ �ν��Ͻ� ȹ��
	private AuthClientHandler authHandler = AuthClientHandler.getInstance();

	private ServerSocket serverSocket;
	private Socket socket;

	public OIOServer() {
		try {

			serverSocket = new ServerSocket(ServerConst.PORT_NUM);
			System.out.println("��������...");

			conManagerager.start();
			System.out.println("�Ŵ�������...");

			while (true) {
				// ���ŷ ����
				socket = serverSocket.accept();
				// ��Ʈ���� ���� Ÿ�Ӿƿ� ����
				// socket.setSoTimeout(Const.STREAM_TIME_OUT);
				System.out.println("������ ���� ����");
				authHandler.authClientAndDelegate(socket);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("�������� ���� : " + e.getMessage());
		} finally {
			try {
				socket.close();
				serverSocket.close();
				conManagerager.closeAll();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
