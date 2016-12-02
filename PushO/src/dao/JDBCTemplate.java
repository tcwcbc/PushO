package dao;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import exception.EmptyResultDataException;
import model.UserAuth;
import res.Const;
import server.SetPrepareStatement;

public class JDBCTemplate {
	private Connection con = null;
	private PreparedStatement ps = null;
	private ResultSet rs;

	public JDBCTemplate() {
		if (con == null) {
			connectDB();
		}
	}

	private void connectDB() {
		try {
			Class.forName(Const.CLASS_FOR_NAME);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			con = DriverManager.getConnection(Const.JDBC_URL+Const.DB_NAME,
									Const.DB_USER_ID, Const.DB_USER_PASSWORD);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public UserAuth executeQuery(String sql, SetPrepareStatement pstm) throws EmptyResultDataException {
		UserAuth userAuthResult = null;
		try {
			ps = con.prepareStatement(sql);
			
			pstm.setFields(ps);

			rs = ps.executeQuery();
			if (rs.next()) {
				userAuthResult = new UserAuth();
				userAuthResult.setId(rs.getInt("id"));
				userAuthResult.setName(rs.getString("name"));
				userAuthResult.setIp(rs.getString("ip"));
				userAuthResult.setPort(rs.getInt("port"));
				
			}
			closeDBSet();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		if (userAuthResult == null) {
			throw new EmptyResultDataException("등록된 사용자 아님");
		}
		return userAuthResult;
	}

	private void closeDBSet() throws SQLException {
		rs.close();
		ps.close();
		con.close();
	}
}
