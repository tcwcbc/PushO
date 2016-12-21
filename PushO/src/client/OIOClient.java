package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;

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

	public int num =0;
	public String passwd;
	public OIOClient() {
		// TODO Auto-generated constructor stub
	}
	public OIOClient(int num,String passwd) {
		// TODO Auto-generated constructor stub
		this.num = num;
		this.passwd = passwd;
	}
	// ������ ���� �۾�
	public boolean connectServer() {
		boolean isServerSurvival = false;
		try {
			if (socket != null && socket.isConnected()) {
				close();
				ClientConst.CLIENT_LOGGER.debug("Re-try Server Connection");
			}

			socket = new Socket(ClientConst.SERVER_IP, ClientConst.PORT_NUM);
			ClientConst.CLIENT_LOGGER.info("Socket information : [{}]",socket);
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());

			// Ű��ȯ�� �̷����� �۾�
			KeyExchangeClient key = new KeyExchangeClient(bis, bos);
			aesKey = key.start();
			ClientConst.CLIENT_LOGGER.info("Complete Key Exchange, AESKey : [{}]", aesKey);

			isServerSurvival = true;

			CilentDataProcess.sendAuth(bos, aesKey, num, passwd);
			CilentDataProcess.receive(socket, bis, bos, aesKey);

			return true;
		} catch (IOException e) {
			if (isServerSurvival == false) {
				ClientConst.CLIENT_LOGGER.error("Server Connection Exception!!");
				return false;
			} else {
				ClientConst.CLIENT_LOGGER.error("No Server Response !!");
				try {
					// ������ ���� JSON �޼��� ����
					CilentDataProcess.sendAuth(bos, aesKey,num,passwd);
					ClientConst.CLIENT_LOGGER.debug("Re-send Auth Message");
					CilentDataProcess.receive(socket, bis, bos, aesKey);
				} catch (IOException e1) {
					ClientConst.CLIENT_LOGGER.error("No Server Response!!");
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
					ClientConst.CLIENT_LOGGER.error("Time out Exception!!");
					continue;
				}
			}
		}
	}
}