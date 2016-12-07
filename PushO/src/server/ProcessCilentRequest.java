package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import dao.JDBCTemplate;
import exception.EmptyResultDataException;
import res.Const;
import util.Utils;

/**
 * @author �ֺ�ö
 * @Description ������ �� Ŭ���̾�Ʈ�� ��������� �ϸ� �۾��� �����ϴ� ������ TODO ������� ���� ��Ʈ����
 *              ���ڽ�Ʈ��->����Ʈ��Ʈ�� ���� ��ȯ ������ ��ƿŬ���� ������� �޽��� �ۼ���� ���� Ÿ�Ӿƿ� ���ܰ� �߻����� ���
 *              �˸��޽����� �����ϰ� �������� �� �ڿ�ȸ�� ��Ŀ���� ����ó������ ���� ���� try-catch���� ����
 */
public class ProcessCilentRequest extends Thread {

	private Socket connectedSocketWithClient;
	private byte[] msgPingByte;
	private byte[] msgPongByte;
	private byte[] msgPushByte;

	private BufferedOutputStream bos;
	private BufferedInputStream bis;

	public ProcessCilentRequest(Socket socket) {
		this.connectedSocketWithClient = socket;
	}

	@Override
	public void run() {
		try {
			bos = new BufferedOutputStream(connectedSocketWithClient.getOutputStream());
			bis = new BufferedInputStream(connectedSocketWithClient.getInputStream());

			/**
			 * ���� �����͸� �ְ� �޴� �κ� TODO : �����Ӹ� �ƴ϶� ���� �����͸� �ְ�޴� ����
			 */
			startPingPong();
		} catch (SocketTimeoutException timeoutE) {
			// TODO Ÿ�Ӿƿ� �߻� �ÿ� �ڿ�ȸ�� �� ó�� ��Ŀ����
			timeoutE.printStackTrace();
			try {
				bos.write(msgPingByte);
				System.out.println("Ping ����");

			} catch (IOException e) {
				e.printStackTrace();
				try {
					bis.close();
					bos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void startPingPong() throws IOException, InterruptedException {
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

		bos.write(msgPingByte);
		bos.flush();
		System.out.println("������ ���� Ping ����");

		while ((readCount = bis.read(buf)) != -1) {
			length = Utils.byteToInt(buf);
			body = new byte[length];
			bodylength = bis.read(body);
			String pp = Utils.parseJSONMessage(new JSONParser(), new String(body));
			if (pp.equals(Const.JSON_VALUE_PING)) {
				bos.write(msgPongByte);
				System.out.println("Pong ����");
			}
			if (pp.equals(Const.JSON_VALUE_PONG)) {
				bos.write(msgPingByte);
				System.out.println("Ping ����");
			}
			bos.flush();
		}
	}

	/**
	 * Ŭ���̾�Ʈ���� �˸��� �����ϴ� �޼ҵ� String �����͸� �����ڷ� split�Ͽ� ������ ���Ŀ� �°� �����Ѵ�.
	 * 
	 * @param msg
	 *            �ֹ�����
	 */
	public void setPush(String msg) {
		try {
			msgPushByte = Utils.makeMessageStringToByte(new byte[Const.HEADER_LENTH + msg.getBytes().length],
					msg);

			bos.write(msgPushByte);
			bos.flush();
			System.out.println("Ǫ���Ϸ�:" + this.getName());
		} catch (IOException e) {
			// ��� Ŭ���̾�Ʈ ������ �������� �߻� 
			// �׿� ���� HashMap�� ����Ǿ��ִ� ���� Thread�� ����� �۾��� �ʿ���
			System.out.println("setPush() Ǫ���߼��� ����" + e.getMessage());
		}
	}
}
