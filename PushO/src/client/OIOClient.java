package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.encry.KeyExchangeClient;
import client.res.ClientConst;
import client.util.ClientUtils;
import server.model.PushInfo;

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

	private int readCount;
	private int dataSize;
	private int bodyLength;

	public String desKey;

	// ������ ���� �۾�
	public boolean connectServer() {
		boolean isServerSurvival = false;
		try {
			socket = new Socket(ClientConst.SERVER_IP, ClientConst.PORT_NUM);
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());

			// Ű��ȯ�� �̷����� �۾�
			KeyExchangeClient key = new KeyExchangeClient(bis, bos);
			desKey = key.start();
			System.out.println("Ű ��ȯ�۾� �Ϸ�:" + desKey);

			isServerSurvival = true;

			// ������ ���� JSON �޼��� ����
			String msgAuthString = ClientUtils.makeJSONMessageForAuth("�Ǹ���10", "��й�ȣ~?", new JSONObject(),
					new JSONObject());
			byte[] msgAuthByte = ClientUtils.makeMessageStringToByte(
					new byte[ClientConst.HEADER_LENTH + msgAuthString.getBytes(ClientConst.CHARSET).length],
					msgAuthString);
			bos.write(msgAuthByte);
			bos.flush();
			System.out.println("���� �޽��� ����");

			// �Է½�Ʈ���� ���� 7�� Ÿ�Ӿƿ� ����
			// socket.setSoTimeout(ClientConst.SEND_WATING_TIME);
			// �޽��� DATASIZE
			byte[] header = new byte[ClientConst.HEADER_LENTH];
			while ((readCount = bis.read(header)) != -1) {
				// ���ŵ� �޽��� DATASIZE
				dataSize = ClientUtils.byteToInt(header);
				// DATA ���̸�ŭ byte�迭 ����
				byte[] body = new byte[dataSize];
				bodyLength = bis.read(body);
				String msg = ClientUtils.parseJSONMessage(new JSONParser(), new String(body, ClientConst.CHARSET));
				// Pong �޽��� �� ���
				if (msg.equals(ClientConst.JSON_VALUE_PONG)) {
					System.out.println("���� ����");
					return true;
				}
			} // end of while
			return false;
		} catch (IOException e) {
			if (isServerSurvival == false) {
				System.out.println("Server Connection Exception �߻�!!");
				return false;
			} else {
				System.out.println("No Server Response �߻�!!");
				return false;
			}
		}
	}

	// �޽��� �ۼ��� �޼ҵ�
	public void processMsg() {
		boolean status = true;

		// �޽��� DATASIZE
		byte[] header = new byte[ClientConst.HEADER_LENTH];
		// ���� �����͸� �ְ� �޴� �κ� TODO : �����Ӹ� �ƴ϶� ���� �����͸� �ְ�޴� ����
		while (status) {
			try {
				// �Է½�Ʈ���� ���� 7�� Ÿ�Ӿƿ� ����
				// socket.setSoTimeout(ClientConst.SEND_WATING_TIME);
				while ((readCount = bis.read(header)) != -1) {
					// ���ŵ� �޽��� DATASIZE
					dataSize = ClientUtils.byteToInt(header);
					// DATA ���̸�ŭ byte�迭 ����
					byte[] body = new byte[dataSize];
					bodyLength = bis.read(body);
					String msg = ClientUtils.parseJSONMessage(new JSONParser(), new String(body, ClientConst.CHARSET));

					// Ping �޽��� �� ���
					if (msg.equals(ClientConst.JSON_VALUE_PING)) {
						String msgPongString = ClientUtils.makeJSONMessageForPingPong(new JSONObject(), false);
						byte[] msgPongByte = ClientUtils.makeMessageStringToByte(
								new byte[ClientConst.HEADER_LENTH + msgPongString.getBytes(ClientConst.CHARSET).length],
								msgPongString);
						bos.write(msgPongByte);
						System.out.println("pong ����");
					}
					// Push �޽��� �� ���
					else if (msg.equals(ClientConst.JSON_VALUE_PUSH)) {
						pushData = ClientUtils.parsePushMessage(new JSONParser(), new String(body, ClientConst.CHARSET),
								pushData);
						System.out.println("�ֹ���ȣ:" + pushData.getOrder_num() + "Ȯ�εǾ����ϴ�.");

						String msgPushResponse = ClientUtils.makeJSONMessageForPush("sucess", new JSONObject(),
								new JSONObject());
						
					}
				} // end of while
			} catch (IOException e) {
				try {
					System.out.println("Time out �߻�...");
					String msgPingString = ClientUtils.makeJSONMessageForPingPong(new JSONObject(), true);
					byte[] msgPingByte = ClientUtils.makeMessageStringToByte(
							new byte[ClientConst.HEADER_LENTH + msgPingString.getBytes(ClientConst.CHARSET).length],
							msgPingString);
					bos.write(msgPingByte);
					bos.flush();
					System.out.println("ping ����");

					// �Է½�Ʈ���� ���� 7�� Ÿ�Ӿƿ� ����
					// socket.setSoTimeout(ClientConst.SEND_WATING_TIME);
					while ((readCount = bis.read(header)) != -1) {
						// ���ŵ� �޽��� DATASIZE
						dataSize = ClientUtils.byteToInt(header);
						// DATA ���̸�ŭ byte�迭 ����
						byte[] body = new byte[dataSize];
						bodyLength = bis.read(body);
						String msg = ClientUtils.parseJSONMessage(new JSONParser(),
								new String(body, ClientConst.CHARSET));
						// Pong �޽��� �� ���
						if (msg.equals(ClientConst.JSON_VALUE_PONG)) {
							System.out.println("pong ����");
						}
					} // end of while

				} catch (IOException e1) {
					System.out.println("Time out �߻�...");
					// ������ ���� ���
					boolean flag = true;
					while (flag) {
						if (connectServer()) {
							processMsg();
							flag = false;
						}
					}
				}
			}
		} // end of while
	} // end of processMsg()

	public static void main(String[] args) {
		OIOClient mcc = new OIOClient();
		boolean flag = true;
		while (flag) {
			if (mcc.connectServer()) {
				mcc.processMsg();
				flag = false;
			}
		}
	}
}