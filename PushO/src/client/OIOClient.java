package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
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

	public static void main(String[] args) {
		new OIOClient();
	}

	private BufferedOutputStream bos;
	private Socket socket;
	private BufferedInputStream bis;
	private byte[] msgPingByte;
	private byte[] msgPongByte;
	private List<PushInfo> pushList = new ArrayList<>();

	public OIOClient() {
		try {
			socket = new Socket(Const.SERVER_IP, Const.PORT_NUM);
			// �Է½�Ʈ���� ���� Ÿ�Ӿƿ� ����
			// socket.setSoTimeout(Const.STREAM_TIME_OUT);
			bos = new BufferedOutputStream(socket.getOutputStream());
			bis = new BufferedInputStream(socket.getInputStream());
			/*
			 * ���� ������ ���� �޽����� ������ �۽��ϴ� �κ� TODO : ��ȣȭ Ű ��ȯ, ����Ű ����, ����ũ�� ��ȯ ����
			 * �ʱ�ȭ �۾�
			 */
			String msgAuthString = Utils.makeJSONMessageForAuth("root", "root", new JSONObject(), new JSONObject());
			byte[] msgAuthByte = Utils.makeMessageStringToByte(
					new byte[Const.HEADER_LENTH + msgAuthString.getBytes().length], msgAuthString);
			bos.write(msgAuthByte);
			bos.flush();
			System.out.println("���� ����" + new String(msgAuthByte));
			/**
			 * ���� �����͸� �ְ� �޴� �κ� TODO : �����Ӹ� �ƴ϶� ���� �����͸� �ְ�޴� ����
			 */
			String msgPingString = Utils.makeJSONMessageForPingPong(new JSONObject(), true);
			msgPingByte = Utils.makeMessageStringToByte(new byte[Const.HEADER_LENTH + msgPingString.getBytes().length],
					msgPingString);
			String msgPongString = Utils.makeJSONMessageForPingPong(new JSONObject(), false);
			msgPongByte = Utils.makeMessageStringToByte(new byte[Const.HEADER_LENTH + msgPongString.getBytes().length],
					msgPongString);

			byte[] buf = new byte[Const.HEADER_LENTH];
			byte[] body;
			int readCount = 0;
			int length = 0;
			int bodylength = 0;

			while ((readCount = bis.read(buf)) != -1) {
				length = Utils.byteToInt(buf);
				body = new byte[length];
				bodylength = bis.read(body);
				
				String pp = Utils.parseJSONMessage(new JSONParser(), new String(body));
				System.out.println(pp);
				if (pp.equals(Const.JSON_VALUE_PING)) {
					bos.write(msgPongByte);
					System.out.println("Pong ����");
				}
				if (pp.equals(Const.JSON_VALUE_PONG)) {
					bos.write(msgPingByte);
					System.out.println("Ping ����");
				}
				if (pp.equals(Const.JSON_VALUE_PUSH)) {
					pushList = Utils.parsePushMessage(new JSONParser(), new String(body));
					pushList.get(0).showInfo();
					
				}
				bos.flush();
				Thread.sleep(Const.SEND_WATING_TIME);
			}

			// autoPingPong(3);
			// text = br.readLine();
			// autoPingPong(3);

		} catch (SocketTimeoutException timeoutE) {
			timeoutE.printStackTrace();
			// TODO Ÿ�Ӿƿ��� �߻����� ��� ���� ��Ŀ����
			try {
				bos.write(msgPingByte);
				System.out.println("Ping ����");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					bis.close();
					bos.close();
					socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * �׽�Ʈ�� ���� PingPong �޽��� �ۼ��� �޼ҵ�
	 * 
	 * @param time
	 *            �޽����� �����ϰ� ������ ���� Ƚ��
	 * @throws IOException
	 * @throws InterruptedException
	 */
	// private void autoPingPong(int time) throws IOException,
	// InterruptedException {
	// String text;
	// for(int i = 0; i<time; i++){
	// text = br.readLine();
	// Thread.sleep(Const.SEND_WATING_TIME);
	// String pp = Utils.parseJSONMessage(text);
	// if(pp.equals(Const.JSON_VALUE_PING)){
	// bw.write(Utils.makeJSONMessageForPingPong(false));
	// System.out.println("Pong ����");
	// }
	// if(pp.equals(Const.JSON_VALUE_PONG)){
	// bw.write(Utils.makeJSONMessageForPingPong(true));
	// System.out.println("Ping ����");
	// }
	// bw.flush();
	// }
	// }
}
