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
 * @author 최병철
 * @Description 인증을 위한 프록시 클래스로 싱글톤으로 구현 됨 
 * @TODO 싱글톤으로 구현시 멀티쓰레드 환경에서의 동시성 문제 제고
 *              인증을 위한 DB입출력 Blocking 시간 고려
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
	 * 실제로 인증을 수행하는 메소드
	 * 
	 * @param socket
	 *            인증 메시지를 위한 Stream을 얻을 목적의 socket
	 * @return 클라이언트 요청처리 쓰레드
	 * @throws EmptyResultDataException
	 *             등록된 사용자가 아님(인증X)
	 */
	public synchronized void authClientAndDelegate(Socket socket){
		BufferedInputStream bis = null;

		try {
			bis = new BufferedInputStream(socket.getInputStream());

			byte[] buf = new byte[ServerConst.HEADER_LENTH];
			int readCount = 0;
			int length = 0;
			int bodylength = 0;
			System.out.println("바이트 읽기 시작");

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
				
				////매니저에 추가해주는 부분.
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
	 * {@link JDBCTemplate}을 활용한 사용자 인증
	 * 
	 * @param name
	 *            인증을 위한 사용자 이름
	 * @throws EmptyResultDataException
	 *             인증이 안되었을 경우 발생
	 */
	private void checkAuthorization(String name) throws EmptyResultDataException {
		new JDBCTemplate().executeQuery("select * from pj_member where mem_name = ?", new SetPrepareStatement() {
			@Override
			public void setFields(PreparedStatement pstm) throws SQLException {
				System.out.println("디비접속");
				pstm.setString(1, name);
			}
		});
	}
}
