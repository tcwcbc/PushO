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
 * @author 김재우
 * @Description 별도의 쓰레드를 생성하여 사용자 주문테이블을 항상 감시하고 사용자에게 PUSH 해야 할 데이터를
 *              return하고있다.
 */
public class DBThread extends Thread {

	private Pushable pushable;
	public JDBCTemplate db;

	private String msgPushJson;

	private List<PushInfo> pushList = new ArrayList<>();

	public DBThread(Pushable pushable) {
		this.pushable = pushable;
		this.db = new JDBCTemplate();
	}

	@Override
	public void run() {
		while (!this.isInterrupted()) {
			try {

				Thread.sleep(5000);
				pushList = db.executeQuery_ORDER();

				if (ServerUtils.isEmpty(pushList)) {
					System.out.println("푸쉬 데이터 없음");
				} else {
					for (PushInfo orderNum : pushList) {
						orderNum.setOrder_list(db.executeQuery_ORDER_LIST(orderNum.getOrder_num()));
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

	/**
	 * 한개의 주문에 대한 정보들이 들어온다 
	 * @param msg 주문에 대한 정보들
	 */
	public void setPush(PushInfo msg) {
		// 주문정보를 JSON포멧으로 바꾼다.
		msgPushJson = ServerUtils.makeJSONMessageForPush(msg, new JSONObject(), new JSONObject());
		// 알림을 보내기 전에 DB의 알림상태를 전송중으로 바꾼다.
		db.executeQuery_PUSH_STATUS_UPDATE(msg.getOrder_num());
		// 알림메시지를 보낸다.
		pushable.sendPushAll(msgPushJson);
	}
}
