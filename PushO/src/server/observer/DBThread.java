package server.observer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONObject;

import server.dao.JDBCTemplate;
import server.model.PushInfo;
import server.res.ServerConst;
import server.service.AuthClientHandler;
import server.service.ProcessCilentRequest;
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
	
	private Iterator<String> iter;
	public LinkedBlockingQueue<String> receivedAckQueue;

	public DBThread(Pushable pushable, LinkedBlockingQueue<String> receivedAckQueue) {
		this.pushable = pushable;
		this.receivedAckQueue = receivedAckQueue;
		this.db = new JDBCTemplate();
	}

	@Override
	public void run() {
		while (!this.isInterrupted()) {
			try {
				
				//큐에 있는 자료를 다 가저온다
				iter = receivedAckQueue.iterator();
				if(iter.hasNext()){
					//하나씩 꺼내서 DB 상태값을 전송이 완료되었다고 바꾼다
					String receiveOrderNum = iter.next();
					db.executeQuery_PUSH_STATUS_UPDATE(receiveOrderNum, "Y");
				}
				
				pushList = db.executeQuery_ORDER();

				if (ServerUtils.isEmpty(pushList)) {
					ServerConst.SERVER_LOGGER.info("발송할 주문정보 없음");
				} else {
					ServerConst.SERVER_LOGGER.info("발송할 주문정보 " + pushList.size() + "건 검색" );
					for (PushInfo orderNum : pushList) {
						orderNum.setOrder_list(db.executeQuery_ORDER_LIST(orderNum.getOrder_num()));
						//TODO 이 부분은 특정 사용자에게 알림을 보내므로 setPushPartial 바꿔야함 
						setPushAll(orderNum);
					}
				}
				
				// 5초 간격으로 쓰레드가 실행된다.
				Thread.sleep(ServerConst.DB_THREAD_OBSERVER_TIME);
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
	 * HashMap에 등록된 모든사용자에게 알림을 보낼때 사용하는 메소드 
	 * @param msg PushInfo 타입의 주문 정보들
	 */
	public void setPushAll(PushInfo msg) {
		// 주문정보를 JSON포멧으로 바꾼다.
		msgPushJson = ServerUtils.makeJSONMessageForPush(msg, new JSONObject(), new JSONObject());
		// 알림메시지를 보낸다.
		pushable.sendPushAll(msgPushJson);
	}
	
	/**
	 * HashMap에 등록된 특정사용자에게 알림을 보낼때 사용하는 메소드
	 * @param msg PushInfo 타입의 주문 정보들
	 */
	public void setPushPartial(PushInfo msg) {
		msgPushJson = ServerUtils.makeJSONMessageForPush(msg, new JSONObject(), new JSONObject());
		pushable.sendPushPartial(msg.getOrder_seller(), msgPushJson);
	}
	
	
}
