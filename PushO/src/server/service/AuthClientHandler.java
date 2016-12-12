package server.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.json.simple.parser.JSONParser;

import server.dao.JDBCTemplate;
import server.exception.EmptyResultDataException;
import server.observer.DBObserver;
import server.res.ServerConst;
import server.util.ServerUtils;

/**
 * @author �ֺ�ö
 * @Description ������ ���� ���Ͻ� Ŭ������ �̱������� ���� �� 
 * @TODO �̱������� ������ ��Ƽ������ ȯ�濡���� ���ü� ���� ����
 *              ������ ���� DB����� Blocking �ð� ���
 */
public class AuthClientHandler {

	private SocketConnectionManager socketConnectionManagerager = SocketConnectionManager.getInstance();
	private static AuthClientHandler instance = null;

	public static AuthClientHandler getInstance() {
		if (instance == null) {
			instance = new AuthClientHandler();
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
	public synchronized void authClientAndDelegate(Socket socket){
		BufferedInputStream bis = null;

		try {
			bis = new BufferedInputStream(socket.getInputStream());

			byte[] buf = new byte[ServerConst.HEADER_LENTH];
			int readCount = 0;
			int length = 0;
			int bodylength = 0;
			System.out.println("����Ʈ �б� ����");

			readCount = bis.read(buf);
			length = ServerUtils.byteToInt(buf);
			byte[] body = new byte[length];
			bodylength = bis.read(body);
			String text = new String(body, ServerConst.CHARSET);
			System.out.println(text);

			if (text.contains(ServerConst.JSON_VALUE_AUTH)) {
				String name = ServerUtils.parseJSONMessage(new JSONParser(), new String(body, ServerConst.CHARSET));
				boolean authorized = false;
				try{
					checkAuthorization(name);
					authorized = true;
				} catch(EmptyResultDataException e){
					e.printStackTrace();
				}
				
				////�Ŵ����� �߰����ִ� �κ�.
				socketConnectionManagerager.add(name, socket, authorized);
				////
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
		new JDBCTemplate().executeQuery("select * from pj_member where mem_name = ?", new SetPrepareStatement() {
			@Override
			public void setFields(PreparedStatement pstm) throws SQLException {
				System.out.println("�������");
				pstm.setString(1, name);
			}
		});
	}
}
