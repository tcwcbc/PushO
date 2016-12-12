package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.res.ClientConst;
import client.util.ClientUtils;
import server.model.PushInfo;
import server.service.OIOServer;

/**
 * @author �ֺ�ö
 * @Description Ŭ���̾�Ʈ ���α׷�, ������ ���ÿ� ������������ ���� �� �ó������� ���� ��� TODO �ۼ��� �ϴ� ��Ʈ����
 *              ���ڽ�Ʈ������ ����Ʈ��Ʈ������ ��ȯ Ÿ�Ӿƿ��̳� ��Ÿ ���Ṯ�� ���� ��Ŀ���� ���� �� ��Ÿ ����ó�� �̺�Ʈ��
 *              �߻���Ű�ų� �����͸� ���� �� ���������� ������ View
 */
public class OIOClient {
	/////
	private Socket socket;
	private BufferedOutputStream bos;
	private BufferedInputStream bis;

	private PushInfo pushData;

	// ������ ���� �۾�
	public boolean connectServer() {
		try {
			socket = new Socket(ClientConst.SERVER_IP, ClientConst.PORT_NUM);
			
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());

			// ������ ���� JSON �޼��� ����
			String msgAuthString = ClientUtils.makeJSONMessageForAuth("�Ǹ���50", "��й�ȣ~?", new JSONObject(), new JSONObject());
			byte[] msgAuthByte = ClientUtils.makeMessageStringToByte(
					new byte[ClientConst.HEADER_LENTH + msgAuthString.getBytes(ClientConst.CHARSET).length], msgAuthString);
			bos.write(msgAuthByte);
			bos.flush();
			System.out.println("���� ����");
			return true;
		} catch (IOException e) {
			System.out.println("connectServer() Exception �߻�!!");
			return false;
		}
	}

	// �޽��� �ۼ��� �޼ҵ�
	public void processMsg() {
		boolean status = true;
		int readCount = 0;
		int headerLength = 0;
		int bodyLength = 0;

		// ���ŵ� �޽��� DATASIZE
		byte[] header = new byte[ClientConst.HEADER_LENTH];

		// ���� �����͸� �ְ� �޴� �κ� TODO : �����Ӹ� �ƴ϶� ���� �����͸� �ְ�޴� ����
		while (status) {
			try {
				// �Է½�Ʈ���� ���� Ÿ�Ӿƿ� ����
//				socket.setSoTimeout(Const.SEND_WATING_TIME);
				while ((readCount = bis.read(header)) != -1) {
					// ���ŵ� �޽��� DATASIZE
					headerLength = ClientUtils.byteToInt(header);
					// DATA ���̸�ŭ byte�迭 ����
					byte[] body = new byte[headerLength];
					bodyLength = bis.read(body);
					String msg = ClientUtils.parseJSONMessage(new JSONParser(), new String(body, ClientConst.CHARSET));

					// Ping �޽��� �� ���
					if (msg.equals(ClientConst.JSON_VALUE_PING)) {
						String msgPongString = ClientUtils.makeJSONMessageForPingPong(new JSONObject(), false);
						byte[] msgPongByte = ClientUtils.makeMessageStringToByte(
								new byte[ClientConst.HEADER_LENTH + msgPongString.getBytes(ClientConst.CHARSET).length],
								msgPongString);
						bos.write(msgPongByte);
						System.out.println("Pong ����");
					}
					// Push �޽��� �� ���
					else if (msg.equals(ClientConst.JSON_VALUE_PUSH)) {
						pushData = ClientUtils.parsePushMessage(new JSONParser(), new String(body, ClientConst.CHARSET), pushData);
						System.out.println(pushData.getOrder_list().get(0).getProduct().toString());
					}
				} // end of while
			} catch (IOException e) {
				try {
					// Ping �޽��� ����
					System.out.println("Time out �߻�...");
					String msgPingString = ClientUtils.makeJSONMessageForPingPong(new JSONObject(), true);
					byte[] msgPingByte = ClientUtils.makeMessageStringToByte(
							new byte[ClientConst.HEADER_LENTH + msgPingString.getBytes(ClientConst.CHARSET).length], msgPingString);
					bos.write(msgPingByte);
					bos.flush();
					System.out.println("ping ����");

					try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}

					readCount = bis.read(header);

					// ���ŵ� �޽��� DATASIZE
					headerLength = ClientUtils.byteToInt(header);
					// DATA ���̸�ŭ byte�迭 ����
					byte[] body = new byte[headerLength];
					bodyLength = bis.read(body);
					String msg = ClientUtils.parseJSONMessage(new JSONParser(), new String(body, ClientConst.CHARSET));
					// Pong �޽��� �� ���
					if (msg.equals(ClientConst.JSON_VALUE_PONG)) {
						System.out.println("Pong ����");
					}
				} catch (IOException e1) {
					// ������ ���� ���
					boolean flag = true;
					while (flag) {
						if (connectServer()) {
							processMsg();
							flag = false;
						} else {
							System.out.println("���� ���۵��� ...");
						}
					}

				}
			}
		} // end of while
	} // end of processMsg()

	public static void main(String[] args) {
		OIOClient mcc = new OIOClient();
		mcc.connectServer();
		mcc.processMsg();
	}
}