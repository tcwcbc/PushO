package server.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.json.simple.parser.JSONParser;

import server.dao.JDBCTemplate;
import server.encry.AESUtils;
import server.encry.KeyExchangeServer;
import server.exception.AlreadyConnectedSocketException;
import server.exception.EmptyResultDataException;
import server.res.ServerConst;
import server.util.ServerUtils;

/**
 * @author �ֺ�ö
 * @Description ������ ���� ���Ͻ� Ŭ������ �̱������� ���� �� 
 * @TODO �̱������� ������ ��Ƽ������ ȯ�濡���� ���ü� ���� ����
 *              ������ ���� DB����� Blocking �ð� ���
 */
public class AuthClientHandler extends Thread {
	
	private SocketConnectionManager socketConnectionManagerager = SocketConnectionManager.getInstance();
	
	private static AuthClientHandler instance = null;
	
	public static AuthClientHandler getInstance() {
		if (instance == null) {
			instance = new AuthClientHandler();
		}
		return instance;
	}

	@Override
	public void run() {
		while(!this.isInterrupted()){
			try {
				Socket socket = ServerConst.SOCKET_QUEUE.take();

				String aesKey = encryptionKeyChange(socket);
				ServerConst.SERVER_LOGGER.info(socket.getInetAddress().getHostName() + " Ű ��ȯ�۾� �Ϸ�");
				ServerConst.SERVER_LOGGER.debug("��ȣȭ Ű " + aesKey);
				authClientAndDelegate(socket, aesKey);

				System.out.println("���ŷť get : "+socket.getClass().getName());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	private String encryptionKeyChange(Socket socket) {
		KeyExchangeServer kes = new KeyExchangeServer(socket);
		String key = kes.start();
		return key;
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
	public synchronized void authClientAndDelegate(Socket socket, String aesKey){
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());

			byte[] buf = new byte[ServerConst.HEADER_LENTH];
			int readCount = 0;
			int length = 0;
			int bodylength = 0;
			System.out.println("����Ʈ �б� ����");

			readCount = bis.read(buf);
			length = ServerUtils.byteToInt(buf);
			byte[] body = new byte[length];
			bodylength = bis.read(body);
			String msg = new String(body, ServerConst.CHARSET);
			msg = AESUtils.AES_Decode(msg, aesKey);
			System.out.println(msg);

			if (msg.contains(ServerConst.JSON_VALUE_AUTH)) {
				String name = ServerUtils.parseJSONMessage(new JSONParser(), msg);
				boolean authorized = false;
				try{
					//���� ���� �� ���ܰ� �߻��Ǵ� �κ�
					checkAuthorization(name);
					authorized = true;
					if(authorized){
						////�Ŵ����� �߰����ִ� �κ�.
						socketConnectionManagerager.addClientSocket(name, socket, aesKey);
					}
				} catch(EmptyResultDataException e){
					//TODO Ŭ���̾�Ʈ���� �������� �ʾҴٴ� �޽����� ����
//					bos.write(b);
					socket.close();
					e.printStackTrace();
				} catch(AlreadyConnectedSocketException e){ 
					//TODO Ŭ���̾�Ʈ���� �̹� ����� ���̵��� �޽����� ����
//					bos.write(b);
					socket.close();
					e.printStackTrace();
				}
				
				
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
		new JDBCTemplate().executeQuery("select * from pj_member where mem_name = ?", 
				new SetPrepareStatement() {
			@Override
			public void setFields(PreparedStatement pstm) throws SQLException {
				System.out.println("�������");
				pstm.setString(1, name);
			}
		});
	}
}
