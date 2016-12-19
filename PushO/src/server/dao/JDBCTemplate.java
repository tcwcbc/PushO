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
import server.model.OrderInfo;
import server.model.UserAuth;
import server.res.ServerConst;
import server.service.SetPrepareStatement;

/**
 * @author �ֺ�ö
 * @Description �����ͺ��̽��� �����Ͽ� ������ �����ϰ� ������� ��ȯ�ϴ� ���� ��� �۾��� ���� ���� �� DBĿ�ؼ��� �غ��ϰ�
 *              �޼ҵ� ȣ�� �� ������ ���� �޼ҵ� ���ο��� {@link SetPrepareStatement} �ݹ� �������̽���
 *              ����ϰ� Caller �ʿ� {@link EmptyResultDataException}�� ���Ͽ� DB�� ��ϵ��� ����
 *              �����(����X)���� �˸� TODO ��Ƽ������ ȯ�濡�� ���ü��� ������ ���� DBĿ�ؼ� Ǯ ���� �ټ��� Ŭ���̾�Ʈ��
 *              ���ÿ� ���� ���� �� �߻��� �� �ִ� ����ȭ ����
 */
public class JDBCTemplate {
	private Connection con = null;
	private PreparedStatement ps = null;
	private ResultSet rs;

	private List<OrderInfo> pushList = new ArrayList<>();
	private List<ProductList> productList = new ArrayList<>();
	private PushInfo pushInfo;

	public JDBCTemplate() {
		if (con == null) {
			connectDB();
		}
	}

	private void connectDB() {
		try {
			Class.forName(ServerConst.CLASS_FOR_NAME);
			con = DriverManager.getConnection(ServerConst.JDBC_URL + ServerConst.DB_NAME, ServerConst.DB_USER_ID,
					ServerConst.DB_USER_PASSWORD);

		} catch (SQLException | ClassNotFoundException e) {

			e.printStackTrace();
		}
	}

	/**
	 * sql���� �ش� �ʵ带 �����ϸ� ����� ������ Model ��ü�� �����Ͽ� ��ȯ�ϴ� �޼ҵ� ����� ���� ���ܸ� �߻����� ��ϵ��� ����
	 * �����(����X)���� �˸�
	 * 
	 * @param sql
	 *            ������ sql��
	 * @param pstm
	 *            �ݹ� �������̽��� sql���� '?'�κп� ä���� �ʵ带 ����
	 * @return DB�� ����� ������ �� ��ü�� �����Ͽ� ��ȯ
	 * @throws EmptyResultDataException
	 *             ��ϵ��� ���� �����(����X)
	 * 
	 *             TODO ����� {@link UserAuth}�� ����Ͽ� ������ ���� �������� �����Ǿ� �������� ����.
	 */
	public UserAuth executeQuery(String sql, SetPrepareStatement pstm) throws EmptyResultDataException {
		UserAuth userAuthResult = null;
		try {
			ps = con.prepareStatement(sql);
			// �ݹ� �������̽� ȣ��
			pstm.setFields(ps);

			rs = ps.executeQuery();

			// ��� ���� ���ٸ� ������ ������ ������� �ʴ´�.
			if (rs.next()) {
				userAuthResult = new UserAuth(rs.getString("mem_id"), rs.getString("mem_salt"),
						rs.getString("mem_pwd"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			/*
			 * try { //closeDBSet(); } catch (SQLException e) {
			 * e.printStackTrace(); }
			 */
		}
		// ������ ���� ������ �߻�
		if (userAuthResult == null) {
			throw new EmptyResultDataException("Not Registered Client!");
		}
		return userAuthResult;
	}

	/**
	 * TB_USER_ORDER ���̺��� SELECT�ϰ� �ֹ������� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param �ֹ����̺���
	 *            order_push �Ӽ��� N �ΰ��� SELECT�Ѵ�.
	 * @return ��ǰ �󼼸���� ������ �����͸� ��ȯ�Ѵ�.
	 */
	public List<OrderInfo> executeQuery_ORDER() {
		String sql = "SELECT A.oi_no, B.mem_id ,C.seller_id ,A.oi_orderdate, A.oi_totalprice "
				+ "FROM pj_orderinfo AS A INNER JOIN pj_member AS B "
				+ "INNER JOIN (SELECT A.mem_id as 'seller_id', B.st_no   "
				+ "FROM pj_member AS A INNER JOIN pj_store AS B "
				+ "ON A.mem_no = B.mem_no) AS C "
				+ "ON A.mem_no = B.mem_no "
				+ "AND A.oi_push = 'N' AND A.st_no = C.st_no "
						+ "ORDER BY A.oi_no";
		pushList.clear();
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();

			while (rs.next()) {
				pushList.add(new OrderInfo(rs.getString("oi_no"), rs.getString("oi_orderdate"), rs.getString("mem_id"),
						rs.getString("seller_id"), rs.getString("oi_totalprice")));
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pushList;
	}

	/**
	 * TB_USER_ODER_LIST �� TB_PRODUCT ���̺��� INNER JOIN�Ͽ� �ֹ� ��ȣ�� ���� ��ǰ �̸��� ������ ���ϴ�
	 * �޼ҵ�
	 * 
	 * @param sql
	 * @return ��ǰ�̸��� ������ �ϳ��� ��Ʈ������ ��ȯ�ȴ�.
	 */
	public List<ProductList> executeQuery_ORDER_LIST(String orderNum) {
		String sql = "SELECT a.op_productcnt, b.pd_name "
				+ "FROM pj_orderproduct  AS a INNER JOIN pj_product  AS b " + "ON a.oi_no = '" + orderNum
				+ "' AND a.pd_no  = b.pd_no;";
		productList.clear();
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();

			while (rs.next()) {
				productList.add(new ProductList(rs.getString("op_productcnt"), rs.getString("pd_name")));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return productList;
	}

	public PushInfo executeQuery_STOCK() {
		String sql = "SELECT DISTINCT pd_name, pd_stock "
				+ "FROM pj_product "
				+ "WHERE pd_stock < '6' AND st_no = '16';";
		pushInfo = null;
		productList.clear();
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();

			while (rs.next()) {
				productList.add(new ProductList(rs.getString("pd_stock"), rs.getString("pd_name")));
			}
			
			if (!productList.isEmpty()) {
				pushInfo = new PushInfo(productList);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pushInfo;
	}

	/**
	 * 
	 * @param orderNum
	 *            �ֹ���ȣ
	 * @param msg
	 *            �ٲܳ���
	 */
	public void executeQuery_PUSH_STATUS_UPDATE(String orderNum, String msg) {
		String sql = "UPDATE pj_orderinfo set oi_push = '" + msg + "' where oi_no = '" + orderNum + "'";
		try {
			ps = con.prepareStatement(sql);
			ps.executeUpdate();
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
