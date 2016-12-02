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

public class AuthClientProxy {

	private static AuthClientProxy instance = null;

	public static AuthClientProxy getInstance() {
		if (instance == null) {
			instance = new AuthClientProxy();
		}
		return instance;
	}

	public synchronized ProcessCilentRequest getClientSocketThread(Socket socket) throws EmptyResultDataException {
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
