package dao;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Util;

import exception.EmptyResultDataException;
import model.PushInfo;
import model.UserAuth;
import res.Const;
import server.SetPrepareStatement;
import util.Utils;

public class JDBCTemplate {
	private Connection con = null;
	private PreparedStatement ps = null;
	private ResultSet rs;
	
	private List<PushInfo> pushList = new ArrayList<>();

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
			System.out.println("DB연결 완료");
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
	
	public List<PushInfo> executeQuery_ORDER(String sql) {
		pushList.clear();
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			
			while (rs.next()) {
				pushList.add(new PushInfo(rs.getString("order_num"), rs.getString("order_date"),
						rs.getString("order_user"), rs.getString("order_seller"), rs.getString("order_price")));
			} 
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return pushList;
	}
	
	public String executeQuery_ORDER_LIST(String sql) {
		String orderList = "";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			
			while (rs.next()) {
				orderList += "(" + rs.getString("orderlist_count") + "/" + rs.getString("product_name") + ")";
			} 
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return orderList;
		
	}
	

	public void closeDBSet() throws SQLException {
		rs.close();
		ps.close();
		con.close();
	}
}
