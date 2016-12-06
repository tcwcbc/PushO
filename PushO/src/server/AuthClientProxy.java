package server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.json.simple.parser.JSONParser;

import dao.JDBCTemplate;
import exception.EmptyResultDataException;
import observer.DBObserver;
import res.Const;
import util.Utils;

/**
 * @author �ֺ�ö
 * @Description ������ ���� ���Ͻ� Ŭ������ �̱������� ���� �� TODO �̱������� ������ ��Ƽ������ ȯ�濡���� ���ü� ���� ����
 *              ������ ���� DB����� Blocking �ð� ���
 */
public class AuthClientProxy {

	private static AuthClientProxy instance = null;

	public static AuthClientProxy getInstance() {
		if (instance == null) {
			instance = new AuthClientProxy();
		}
		return instance;
	}

	/**
	 * ������ ������ �����ϴ� �޼ҵ�
	 * 
	 * @param socket
	 *            ���� �޽����� ���� Stream�� ���� ������ socket
	 * @return Ŭ���̾�Ʈ ��ûó�� ������
	 * @throws EmptyResultDataException
	 *             ��ϵ� ����ڰ� �ƴ�(����X)
	 */
	public synchronized ProcessCilentRequest getClientSocketThread(Socket socket, DBObserver ob)
			throws EmptyResultDataException {
		ProcessCilentRequest thread = null;
		BufferedInputStream bis = null;

		try {
			bis = new BufferedInputStream(socket.getInputStream());
			byte[] buf = new byte[Const.HEADER_LENTH];
			int readCount = 0;
			int length = 0;
			int bodylength = 0;
			System.out.println("����Ʈ �б� ����");

			readCount = bis.read(buf);
			length = Utils.byteToInt(buf);
			byte[] body = new byte[length];
			bodylength = bis.read(body);
			String text = new String(body);
			System.out.println(text);

			if (text.contains(Const.JSON_VALUE_AUTH)) {
				String name = Utils.parseJSONMessage(new JSONParser(), new String(body));
				// HashMap�� ���� ����� ���� ����
				ob.setUser(name);
				checkAuthorization(name);

				thread = new ProcessCilentRequest(socket);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				bis.close();
			} catch (IOException closeE) {
				// TODO Auto-generated catch block
				closeE.printStackTrace();
			}
		}
		return thread;
	}

	/**
	 * {@link JDBCTemplate}�� Ȱ���� ����� ����
	 * 
	 * @param name
	 *            ������ ���� ����� �̸�
	 * @throws EmptyResultDataException
	 *             ������ �ȵǾ��� ��� �߻�
	 */
	private void checkAuthorization(String name) throws EmptyResultDataException {
		new JDBCTemplate().executeQuery("select * from pj_member where mem_id = ?", new SetPrepareStatement() {
			@Override
			public void setFields(PreparedStatement pstm) throws SQLException {
				System.out.println("�������");
				pstm.setString(1, name);
			}
		});
	}
}
