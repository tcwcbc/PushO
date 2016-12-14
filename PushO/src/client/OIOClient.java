package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;

import client.encry.AESUtils;
import client.encry.KeyExchangeClient;

import client.res.ClientConst;

/**
 * @author �ֺ�ö
 * @Description Ŭ���̾�Ʈ ���α׷�, ������ ���ÿ� ������������ ���� �� �ó������� ���� ��� TODO �ۼ��� �ϴ� ��Ʈ����
 *              ���ڽ�Ʈ������ ����Ʈ��Ʈ������ ��ȯ Ÿ�Ӿƿ��̳� ��Ÿ ���Ṯ�� ���� ��Ŀ���� ���� �� ��Ÿ ����ó�� �̺�Ʈ��
 *              �߻���Ű�ų� �����͸� ���� �� ���������� ������ View
 */
public class OIOClient {
	private Socket socket;
	private BufferedOutputStream bos;
	private BufferedInputStream bis;

	private Timer timer;
	private ClientHeartBeat heartBeat;
	public String aesKey;
	final int timeInterval = 10000;

	// ������ ���� �۾�
	public boolean connectServer() {
		boolean isServerSurvival = false;
		try {
			if (socket != null && socket.isConnected()) {
				close();
				System.out.println("Server RE-Connection �õ�");
			}

			socket = new Socket(ClientConst.SERVER_IP, ClientConst.PORT_NUM);
			System.out.println("Socket ����: " + socket);
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());

			// Ű��ȯ�� �̷����� �۾�
			KeyExchangeClient key = new KeyExchangeClient(bis, bos);
			aesKey = key.start();
			System.out.println("Ű ��ȯ�۾� �Ϸ�:" + aesKey);

			isServerSurvival = true;

			CilentDataProcess.sendAuth(bos, aesKey);
			
			CilentDataProcess.receive(socket, bis, bos, aesKey);

			return true;
		} catch (IOException e) {
			if (isServerSurvival == false) {
				System.out.println("Server Connection Exception �߻�!!");
				return false;
			} else {
				System.out.println("No Server Response �߻�!!");
				try {
					// ������ ���� JSON �޼��� ����
					CilentDataProcess.sendAuth(bos, aesKey);
					System.out.println("���� �޽��� �ٽ� ����");
					CilentDataProcess.receive(socket, bis, bos, aesKey);
				} catch (IOException e1) {
					System.out.println("No Server Response �߻�!!");
					return false;
				}
				return true;
			}
		}
	}

	// �޽��� �ۼ��� �޼ҵ�
	public void processMsg() throws IOException {
		boolean status = true;

		timer = new Timer();
		heartBeat = new ClientHeartBeat(bos, aesKey);
		timer.scheduleAtFixedRate(heartBeat, timeInterval, timeInterval);

		while (status) {
			try {
				CilentDataProcess.receive(socket, bis, bos, aesKey);
			} catch (IOException e) {
				CilentDataProcess.occurTimeout(socket, bis, bos, aesKey);
			}
		}
	}

	public void close() {
		try {
			heartBeat = null;
			timer.cancel();
			bis.close();
			bos.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		OIOClient mcc = new OIOClient();
		boolean flag = true;
		while (flag) {
			if (mcc.connectServer()) {
				try {
					mcc.processMsg();
				} catch (IOException e) {
					System.out.println("Time out �߻�...");
					continue;
				}
			}
		}
	}
}