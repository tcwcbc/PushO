package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import model.PushInfo;
import res.Const;
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
			// �Է½�Ʈ���� ���� Ÿ�Ӿƿ� ����
			//socket.setSoTimeout(Const.STREAM_TIME_OUT);

			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());
			String msgAuthString = Utils.makeJSONMessageForAuth("root", "root", new JSONObject(), new JSONObject());
			byte[] msgAuthByte = Utils.makeMessageStringToByte(
					new byte[Const.HEADER_LENTH + msgAuthString.getBytes().length], msgAuthString);
			bos.write(msgAuthByte);
			bos.flush();
			System.out.println("���� ����");
			processMsg();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void processMsg() {
		boolean status = true;
		int readCount = 0;
		int headerLength = 0;
		int bodyLength = 0;
		// ���ŵ� �޽��� DATASIZE
		byte[] header = new byte[Const.HEADER_LENTH];
		/**
		 * ���� �����͸� �ְ� �޴� �κ� TODO : �����Ӹ� �ƴ϶� ���� �����͸� �ְ�޴� ����
		 */
		while (status) {
			try {
				// timeout ����
				//socket.setSoTimeout(Const.SEND_WATING_TIME);
				while ((readCount = bis.read(header)) != -1) {

					// ���ŵ� �޽��� DATASIZE
					headerLength = Utils.byteToInt(header);
					// DATA ���̸�ŭ byte�迭 ����
					byte[] body = new byte[headerLength];
					bodyLength = bis.read(body);
					System.out.println(new String(body));
					String msg = Utils.parseJSONMessage(new JSONParser(), new String(body));

					// Ping �޽��� �� ���
					if (msg.equals(Const.JSON_VALUE_PING)) {
						String msgPongString = Utils.makeJSONMessageForPingPong(new JSONObject(), false);
						byte[] msgPongByte = Utils.makeMessageStringToByte(
								new byte[Const.HEADER_LENTH + msgPongString.getBytes().length], msgPongString);
						bos.write(msgPongByte);
						System.out.println("Pong ����");
					}
					// Push �޽��� �� ���
					else if (msg.equals(Const.JSON_VALUE_PUSH)) {
						pushData = Utils.parsePushMessage(new JSONParser(), new String(body), pushData);
						System.out.println(pushData.getOrder_list().get(0).getProduct().toString());
					}
				} // end of while

			} catch (IOException e) {
				try {
					// Ping �޽��� ����
					System.out.println("Time out �߻�...");
					String msgPingString = Utils.makeJSONMessageForPingPong(new JSONObject(), true);
					byte[] msgPingByte = Utils.makeMessageStringToByte(
							new byte[Const.HEADER_LENTH + msgPingString.getBytes().length], msgPingString);
					bos.write(msgPingByte);
					bos.flush();
					System.out.println("ping ����");
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

				try {
					// ���ŵ� �޽��� DATASIZE
					headerLength = Utils.byteToInt(header);
					// DATA ���̸�ŭ byte�迭 ����
					byte[] body = new byte[headerLength];
					bodyLength = bis.read(body);
					String msg = Utils.parseJSONMessage(new JSONParser(), new String(body));

					// Pong �޽��� �� ���
					if (msg.equals(Const.JSON_VALUE_PONG)) {
						System.out.println("Pong ����");
					}
				} catch (IOException e1) {
					// ������ ���� ���
					boolean flag = true;
					while (flag) {
						if (connectServer()) {
							flag = false;
						}
					}

				}
			}

		}

		// Ŭ���̾�Ʈ�� ���� ���
		System.out.println("[MultiChatClient]" + "�����!");
		status = false;

	}

	public static void main(String[] args) {
		OIOClient mcc = new OIOClient();
		mcc.connectServer();
		mcc.processMsg();
	}
}