package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import model.PushInfo;
import res.Const;
import server.OIOServer;
import util.Utils;

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

	private PushInfo pushData;

	// ������ ���� �۾�
	public boolean connectServer() {
		try {
			socket = new Socket(Const.SERVER_IP, Const.PORT_NUM);

			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());

			// ������ ���� JSON �޼��� ����
			String msgAuthString = Utils.makeJSONMessageForAuth("�ٿ츶Ʈ������", "��й�ȣ~?", new JSONObject(),
					new JSONObject());
			byte[] msgAuthByte = Utils.makeMessageStringToByte(
					new byte[Const.HEADER_LENTH + msgAuthString.getBytes(Const.CHARSET).length], msgAuthString);
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
		byte[] header = new byte[Const.HEADER_LENTH];

		// ���� �����͸� �ְ� �޴� �κ� TODO : �����Ӹ� �ƴ϶� ���� �����͸� �ְ�޴� ����
		while (status) {
			try {
				// �Է½�Ʈ���� ���� Ÿ�Ӿƿ� ����: 7��
				socket.setSoTimeout(Const.SEND_WATING_TIME);
				while ((readCount = bis.read(header)) != -1) {
					// ���ŵ� �޽��� DATASIZE
					headerLength = Utils.byteToInt(header);
					// DATA ���̸�ŭ byte�迭 ����
					byte[] body = new byte[headerLength];
					bodyLength = bis.read(body);
					String msg = Utils.parseJSONMessage(new JSONParser(), new String(body, Const.CHARSET));

					// Ping �޽��� �� ���
					if (msg.equals(Const.JSON_VALUE_PING)) {
						String msgPongString = Utils.makeJSONMessageForPingPong(new JSONObject(), false);
						byte[] msgPongByte = Utils.makeMessageStringToByte(
								new byte[Const.HEADER_LENTH + msgPongString.getBytes(Const.CHARSET).length],
								msgPongString);
						bos.write(msgPongByte);
						System.out.println("Pong ����");
					}
					// Push �޽��� �� ���
					else if (msg.equals(Const.JSON_VALUE_PUSH)) {
						pushData = Utils.parsePushMessage(new JSONParser(), new String(body, Const.CHARSET), pushData);
						System.out.println(pushData.getOrder_list().get(0).getProduct().toString());
					}
				} // end of while
			} catch (IOException e) {
				try {
					// Ping �޽��� ����
					System.out.println("Time out �߻�...");
					String msgPingString = Utils.makeJSONMessageForPingPong(new JSONObject(), true);
					byte[] msgPingByte = Utils.makeMessageStringToByte(
							new byte[Const.HEADER_LENTH + msgPingString.getBytes(Const.CHARSET).length], msgPingString);
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
					headerLength = Utils.byteToInt(header);
					// DATA ���̸�ŭ byte�迭 ����
					byte[] body = new byte[headerLength];
					bodyLength = bis.read(body);
					String msg = Utils.parseJSONMessage(new JSONParser(), new String(body, Const.CHARSET));
					// Pong �޽��� �� ���
					if (msg.equals(Const.JSON_VALUE_PONG)) {
						System.out.println("Pong ����");
					}
				} catch (IOException e1) {
					// ������ ���� ���
					boolean flag = true;
					while (flag) {
						if (connectServer() && !OIOServer.isSurvival()) {
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