package server.observer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import server.dao.JDBCTemplate;
import server.model.PushInfo;
import server.service.Pushable;
import server.util.ServerUtils;

/**
 * 
 * @author �����
 * @Description ������ �����带 �����Ͽ� ����� �ֹ����̺��� �׻� �����ϰ� ����ڿ��� PUSH �ؾ� �� �����͸�
 *              return�ϰ��ִ�.
 */
public class DBThread extends Thread {

	private Pushable pushable;
	public JDBCTemplate db;
	
	private String sql;
	private String msgPushJson;
	
	private List<PushInfo> pushList = new ArrayList<>();



	public DBThread(Pushable pushable) {
		this.pushable = pushable;
		this.db = new JDBCTemplate();
	}

	@Override
	public void run() {
		sql = "SELECT order_num, order_user, order_seller, order_date, order_price "
				+ "FROM TB_USER_ORDER WHERE order_push = 'N'";

		while (!currentThread().isInterrupted()) {
			try {

				Thread.sleep(5000);
				pushList = db.executeQuery_ORDER(sql);

				if (ServerUtils.isEmpty(pushList)) {
					System.out.println("Ǫ�� ������ ����");
				} else {
					for (PushInfo orderNum : pushList) {
						orderNum.setOrder_list(db.executeQuery_ORDER_LIST(getQuery(orderNum.getOrder_num())));
						setPush(orderNum);
					}
				}
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
				try {
					db.closeDBSet();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} finally {
				pushList.clear();
			}
		}
	}

	public void setPush(PushInfo msg) {
		msgPushJson = ServerUtils.makeJSONMessageForPush(msg, new JSONObject(), new JSONObject());
		//System.out.println("���� ������:" + msgPushJson);
		pushable.sendPushAll(msgPushJson);
	}

	public String getQuery(String orderNum) {
		String sql = "SELECT a.orderlist_count, b.product_name "
				+ "FROM TB_USER_ORDER_LIST AS a INNER JOIN TB_PRODUCT AS b " + "ON a.orderlist_num = '" + orderNum
				+ "' AND a.orderlist_product = b.product_num;";

		return sql;
	}

}
