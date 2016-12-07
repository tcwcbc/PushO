package test;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import dao.JDBCTemplate;
import exception.EmptyResultDataException;
import model.UserAuth;
import server.SetPrepareStatement;

public class DBTest {
	public static void main(String[] args) {
		JDBCTemplate template = new JDBCTemplate();
		try {
			UserAuth auth = template.executeQuery("select * from pj_member where mem_name = ?", 
					new SetPrepareStatement() {
						@Override
						public void setFields(PreparedStatement pstm) throws SQLException {
							// TODO Auto-generated method stub
							pstm.setString(1, "다우마트사장");
						}
					});
			System.out.println(auth.toString());
		} catch (EmptyResultDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
}
