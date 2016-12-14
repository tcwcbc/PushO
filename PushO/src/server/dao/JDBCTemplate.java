package server.dao;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import server.exception.EmptyResultDataException;
import server.model.ProductList;
import server.model.PushInfo;
import server.model.UserAuth;
import server.res.ServerConst;
import server.service.SetPrepareStatement;

/**
 * @author		�ֺ�ö
 * @Description	�����ͺ��̽��� �����Ͽ� ������ �����ϰ� ������� ��ȯ�ϴ� ���� ��� �۾��� ����
 * 				���� �� DBĿ�ؼ��� �غ��ϰ� �޼ҵ� ȣ�� �� ������ ����
 * 				�޼ҵ� ���ο��� {@link SetPrepareStatement} �ݹ� �������̽��� ����ϰ� 
 * 				Caller �ʿ� {@link EmptyResultDataException}�� 
 * 				���Ͽ� DB�� ��ϵ��� ���� �����(����X)���� �˸�
 * TODO			��Ƽ������ ȯ�濡�� ���ü��� ������ ���� DBĿ�ؼ� Ǯ ����
 * 				�ټ��� Ŭ���̾�Ʈ�� ���ÿ� ���� ���� �� �߻��� �� �ִ� ����ȭ ����
 */
public class JDBCTemplate {
	private Connection con = null;
	private PreparedStatement ps = null;
	private ResultSet rs;
	
	private List<PushInfo> pushList = new ArrayList<>();
	private List<ProductList> productList = new ArrayList<>();
	
	public JDBCTemplate() {
		if (con == null) {
			connectDB();
		}
	}

	private void connectDB() {
		try {
			Class.forName(ServerConst.CLASS_FOR_NAME);
			con = DriverManager.getConnection(ServerConst.JDBC_URL+ServerConst.DB_NAME,
					ServerConst.DB_USER_ID, ServerConst.DB_USER_PASSWORD);

		} catch (SQLException | ClassNotFoundException e) {

			e.printStackTrace();
		}
	}

	/**
	 * sql���� �ش� �ʵ带 �����ϸ� ����� ������ Model ��ü�� �����Ͽ� ��ȯ�ϴ� �޼ҵ�
	 * ����� ���� ���ܸ� �߻����� ��ϵ��� ���� �����(����X)���� �˸�
	 * @param sql		������ sql��
	 * @param pstm		�ݹ� �������̽��� sql���� '?'�κп� ä���� �ʵ带 ����
	 * @return			DB�� ����� ������ �� ��ü�� �����Ͽ� ��ȯ
	 * @throws EmptyResultDataException	��ϵ��� ���� �����(����X)
	 * 
	 * TODO ����� {@link UserAuth}�� ����Ͽ� ������ ���� �������� �����Ǿ� �������� ����.
	 */
	public UserAuth executeQuery(String sql, SetPrepareStatement pstm) throws EmptyResultDataException {
		UserAuth userAuthResult = null;
		try {
			ps = con.prepareStatement(sql);
			//�ݹ� �������̽� ȣ��
			pstm.setFields(ps);

			rs = ps.executeQuery();
			
			//��� ���� ���ٸ� ������ ������ ������� �ʴ´�.
			if (rs.next()) {
				userAuthResult = new UserAuth(rs.getString("mem_id"),
						rs.getString("mem_name"),rs.getString("mem_pwd"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				closeDBSet();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		//������ ���� ������ �߻�
		if (userAuthResult == null) {
			throw new EmptyResultDataException("��ϵ� ����� �ƴ�");
		}
		return userAuthResult;
	}
	
	/**
	 * TB_USER_ORDER ���̺��� SELECT�ϰ� �ֹ������� ��ȯ�ϴ� �޼ҵ�
	 * @param          �ֹ����̺��� order_push �Ӽ��� N �ΰ��� SELECT�Ѵ�.
	 * @return         ��ǰ �󼼸���� ������ �����͸� ��ȯ�Ѵ�.
	 */
	public List<PushInfo> executeQuery_ORDER() {
		String sql = "SELECT order_num, order_user, order_seller, order_date, order_price "
				+ "FROM TB_USER_ORDER WHERE order_push = 'N'";
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
	
	/**
	 * TB_USER_ODER_LIST �� TB_PRODUCT ���̺��� INNER JOIN�Ͽ� �ֹ� ��ȣ�� ����
	 * ��ǰ �̸��� ������ ���ϴ� �޼ҵ�
	 * @param sql
	 * @return ��ǰ�̸��� ������ �ϳ��� ��Ʈ������ ��ȯ�ȴ�.
	 */
	public List<ProductList> executeQuery_ORDER_LIST(String orderNum) {
		String sql = "SELECT a.orderlist_count, b.product_name "
				+ "FROM TB_USER_ORDER_LIST AS a INNER JOIN TB_PRODUCT AS b " + "ON a.orderlist_num = '" + orderNum
				+ "' AND a.orderlist_product = b.product_num;";
		productList.clear();
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			
			while (rs.next()) {
				productList.add(new ProductList(rs.getString("product_name"), rs.getString("orderlist_count")));
			} 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return productList;
	}
	
	/**
	 * 
	 * @param orderNum  �ֹ���ȣ
	 * @param msg �ٲܳ���
	 */
	public void executeQuery_PUSH_STATUS_UPDATE(String orderNum, String msg) {
		String sql = "UPDATE TB_USER_ORDER set order_push = '"+ msg +"' where order_num = '"+orderNum+"'";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public void closeDBSet() throws SQLException {
		rs.close();
		ps.close();
		con.close();
	}
}
