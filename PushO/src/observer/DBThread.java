package observer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dao.JDBCTemplate;
import model.PushInfo;
import util.Utils;

/**
 * 
 * @author       �����
 * @Description  ������ �����带 �����Ͽ� ����� �ֹ����̺��� �׻� �����ϰ�
 *               ����ڿ��� PUSH �ؾ� �� �����͸� return�ϰ��ִ�.
 */
public class DBThread extends Thread {
	
	private DBObserver ob;
	private boolean DBThread_flag = true;
	
	private JDBCTemplate db;
	private String sql;
	
	private List<PushInfo> pushList = new ArrayList<>();
	
	public DBThread(DBObserver ob) {
		this.ob = ob;
		this.db = new JDBCTemplate();
	}
	
	@Override
	public void run() {
		sql = "SELECT order_num, order_user, order_seller, order_date, order_price "
				+ "FROM TB_USER_ORDER WHERE order_push = 'N'";

		while (DBThread_flag) {
			try {
				
				Thread.sleep(5000);
				pushList = db.executeQuery_ORDER(sql);
				
				if (Utils.isEmpty(pushList)) {
					System.out.println("Ǫ�� ������ ����");
				} else {
					for (PushInfo orderNum : pushList) {
						orderNum.setOrder_list(db.executeQuery_ORDER_LIST(getQuery(orderNum.getOrder_num())));
						ob.msgPush(orderNum.getInfo());
					}
				}
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
				obserberStop();
			} finally {
				pushList.clear();
			}
		}
	}
	

	public String getQuery(String orderNum) {
		String sql = "SELECT a.orderlist_count, b.product_name "
				+ "FROM TB_USER_ORDER_LIST AS a INNER JOIN TB_PRODUCT AS b "
				+ "ON a.orderlist_num = '"+ orderNum +"' AND a.orderlist_product = b.product_num;";
		
		return sql;		
	}
	
	//������ ����
	public void obserberStop() {
		try {
			DBThread_flag = false;
			db.closeDBSet();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
