package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import dao.JDBCTemplate;
import exception.EmptyResultDataException;
import res.Const;
import util.Utils;

/**
 * @author		최병철
 * @Description	인증을 위한 프록시 클래스로 싱글톤으로 구현 됨
 * TODO			싱글톤으로 구현시 멀티쓰레드 환경에서의 동시성 문제 제고
 * 				인증을 위한 DB입출력 Blocking 시간 고려
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
	 * 실제로 인증을 수행하는 메소드
	 * @param socket	인증 메시지를 위한 Stream을 얻을 목적의 socket
	 * @return			클라이언트 요청처리 쓰레드
	 * @throws EmptyResultDataException	등록된 사용자가 아님(인증X)
	 */
	public synchronized ProcessCilentRequest getClientSocketThread(Socket socket) 
													throws EmptyResultDataException {
		ProcessCilentRequest thread = null;
		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String text = br.readLine();
			if (text.contains(Const.JSON_VALUE_AUTH)) {
				checkAuthorization(Utils.parseJSONMessage(text));

				thread = new ProcessCilentRequest(socket);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				br.close();
			} catch (IOException closeE) {
				// TODO Auto-generated catch block
				closeE.printStackTrace();
			}
		}
		return thread;
	}

	/**
	 * {@link JDBCTemplate}을 활용한 사용자 인증
	 * @param name		인증을 위한 사용자 이름
	 * @throws EmptyResultDataException	인증이 안되었을 경우 발생
	 */
	private void checkAuthorization(String name) throws EmptyResultDataException {
		new JDBCTemplate().executeQuery("select * from user_auth where name = ?", 
				new SetPrepareStatement() {
					@Override
					public void setFields(PreparedStatement pstm) throws SQLException {
						// TODO Auto-generated method stub
						pstm.setString(1, name);
					}
				});
	}
}
