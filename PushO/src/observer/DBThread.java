package observer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import dao.JDBCTemplate;
import model.PushInfo;
import res.Const;
import util.Utils;

/**
 * 
 * @author 김재우
 * @Description 별도의 쓰레드를 생성하여 사용자 주문테이블을 항상 감시하고 사용자에게 PUSH 해야 할 데이터를
 *              return하고있다.
 */
public class DBThread extends Thread {

	private DBObserver ob;
	private boolean DBThread_flag = true;

	private JDBCTemplate db;
	
	private String sql;
	private String msgPushJson;
	
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
					System.out.println("푸쉬 데이터 없음");
				} else {
					for (PushInfo orderNum : pushList) {
						orderNum.setOrder_list(db.executeQuery_ORDER_LIST(getQuery(orderNum.getOrder_num())));
						setPush(orderNum);
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

	public void setPush(PushInfo msg) {
		msgPushJson = Utils.makeJSONMessageForPush(msg, new JSONObject(), new JSONObject());
		//System.out.println("전송 데이터:" + msgPushJson);
		ob.msgPush(msgPushJson);
	}

	public String getQuery(String orderNum) {
		String sql = "SELECT a.orderlist_count, b.product_name "
				+ "FROM TB_USER_ORDER_LIST AS a INNER JOIN TB_PRODUCT AS b " + "ON a.orderlist_num = '" + orderNum
				+ "' AND a.orderlist_product = b.product_num;";

		return sql;
	}

	// 쓰레드 종료
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
