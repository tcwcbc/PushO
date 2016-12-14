package server.observer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONObject;

import server.dao.JDBCTemplate;
import server.model.PushInfo;
import server.res.ServerConst;
import server.service.Pushable;
import server.util.ServerUtils;

/**
 * 
 * @author 김재우
 * @Description 별도의 쓰레드를 생성하여 사용자 주문테이블을 항상 감시하고 사용자에게 PUSH 해야 할 데이터를
 *          	SocketConnectionManager로 보내주고있다.    
 */
public class DBThread extends Thread {

	private Pushable pushable;
	public JDBCTemplate db;

	private String msgPushJson;

	private List<PushInfo> pushList = new ArrayList<>();
	
	public LinkedBlockingQueue<PushInfo> receivedAckQueue;

	public DBThread(Pushable pushable, LinkedBlockingQueue<PushInfo> receivedAckQueue) {
		this.pushable = pushable;
		this.receivedAckQueue = receivedAckQueue;
		this.db = new JDBCTemplate();
	}

	@Override
	public void run() {
		while (!this.isInterrupted()) {
			try {
				
				/*
				//TODO ProcessClientRequest에서 넣은 응답받은 큐에 작업이 있을 경우 그것을 가져와 DB에 상태를 갱신하는 로직
				if(this.receivedAckQueue.size()!=0){
					PushInfo receivedAckPushInfo = this.receivedAckQueue.take();
				}*/
				
				Thread.sleep(5000);
				pushList = db.executeQuery_ORDER();

				if (ServerUtils.isEmpty(pushList)) {
					ServerConst.SERVER_LOGGER.info("발송할 주문정보 없음");
				} else {
					ServerConst.SERVER_LOGGER.info("발송할 주문정보 " + pushList.size() + "건 검색" );
					for (PushInfo orderNum : pushList) {
						orderNum.setOrder_list(db.executeQuery_ORDER_LIST(orderNum.getOrder_num()));
						setPushAll(orderNum);
					}
				}
			} catch (InterruptedException e) {
				e.getStackTrace();
				ServerConst.SERVER_LOGGER.error(e.getMessage());
				try {
					db.closeDBSet();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					ServerConst.SERVER_LOGGER.error(e1.getMessage());
				}
			} finally {
				pushList.clear();
			}
		}
	}

	/**
	 * 재고 알림
	 * @param msg 주문에 대한 정보들
	 */
	public void setPushAll(PushInfo msg) {
		// 주문정보를 JSON포멧으로 바꾼다.
		msgPushJson = ServerUtils.makeJSONMessageForPush(msg, new JSONObject(), new JSONObject());
		// 알림메시지를 보낸다.
		pushable.sendPushAll(msgPushJson);
	}
	
	/**
	 * 주문 알림
	 * @param msg
	 */
	public void setPushPartial(PushInfo msg) {
		msgPushJson = ServerUtils.makeJSONMessageForPush(msg, new JSONObject(), new JSONObject());
		pushable.sendPushPartial(msg.getOrder_seller(), msgPushJson);
	}
	
	
}
