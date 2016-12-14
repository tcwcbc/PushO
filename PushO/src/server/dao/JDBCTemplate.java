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
 * @author		최병철
 * @Description	데이터베이스에 접근하여 쿼리를 실행하고 결과값을 반환하는 등의 모든 작업을 수행
 * 				생성 시 DB커넥션을 준비하고 메소드 호출 시 쿼리를 실행
 * 				메소드 내부에서 {@link SetPrepareStatement} 콜백 인터페이스를 사용하고 
 * 				Caller 쪽에 {@link EmptyResultDataException}을 
 * 				통하여 DB에 등록되지 않은 사용자(인증X)임을 알림
 * TODO			멀티쓰레드 환경에서 동시성의 문제를 위한 DB커넥션 풀 적용
 * 				다수의 클라이언트가 동시에 접속 했을 때 발생할 수 있는 동기화 문제
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
	 * sql문과 해당 필드를 설정하면 결과를 가져와 Model 객체에 주입하여 반환하는 메소드
	 * 사용자 정의 예외를 발생시켜 등록되지 않은 사용자(인증X)임을 알림
	 * @param sql		실행할 sql문
	 * @param pstm		콜백 인터페이스로 sql문의 '?'부분에 채워질 필드를 설정
	 * @return			DB에 저장된 값들을 모델 객체에 매핑하여 반환
	 * @throws EmptyResultDataException	등록되지 않은 사용자(인증X)
	 * 
	 * TODO 현재는 {@link UserAuth}을 사용하여 인증을 위한 로직으로 고정되어 유연하지 못함.
	 */
	public UserAuth executeQuery(String sql, SetPrepareStatement pstm) throws EmptyResultDataException {
		UserAuth userAuthResult = null;
		try {
			ps = con.prepareStatement(sql);
			//콜백 인터페이스 호출
			pstm.setFields(ps);

			rs = ps.executeQuery();
			
			//결과 값이 없다면 다음의 문장은 수행되지 않는다.
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
		//인증이 되지 않음을 발생
		if (userAuthResult == null) {
			throw new EmptyResultDataException("등록된 사용자 아님");
		}
		return userAuthResult;
	}
	
	/**
	 * TB_USER_ORDER 테이블을 SELECT하고 주문정보를 반환하는 메소드
	 * @param          주문테이블에서 order_push 속성이 N 인것을 SELECT한다.
	 * @return         상품 상세목록을 제외한 데이터를 반환한다.
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
	 * TB_USER_ODER_LIST 와 TB_PRODUCT 테이블을 INNER JOIN하여 주문 번호에 따른
	 * 상품 이름과 수량을 구하는 메소드
	 * @param sql
	 * @return 상품이름과 갯수가 하나의 스트링으로 반환된다.
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
	 * @param orderNum  주문번호
	 * @param msg 바꿀내용
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
