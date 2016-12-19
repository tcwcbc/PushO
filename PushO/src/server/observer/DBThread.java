package server.observer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONObject;

import server.dao.JDBCTemplate;
import server.model.OrderInfo;
import server.model.PushInfo;
import server.res.ServerConst;
import server.service.AuthClientHandler;
import server.service.ProcessCilentRequest;
import server.service.Pushable;
import server.util.ServerUtils;

/**
 * 
 * @author �����
 * @Description ������ �����带 �����Ͽ� ����� �ֹ����̺��� �׻� �����ϰ� ����ڿ��� PUSH �ؾ� �� �����͸�
 *          	SocketConnectionManager�� �����ְ��ִ�.    
 */
public class DBThread extends Thread {

	private Pushable pushable;
	public JDBCTemplate db;

	private String msgPushJson;

	// �ֹ��˸� ����
	private List<OrderInfo> orderList = new ArrayList<>();
	// ���˸� ����
	private PushInfo pushInfo;
	
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
				// ť�� �����Ͱ� ������ ó���ϴ� �κ�
				// oi_push ���� N -> Y�� ó��
				while(!receivedAckQueue.isEmpty()){
					db.executeQuery_PUSH_STATUS_UPDATE(receivedAckQueue.take(), "Y");
				}
				
				// �ֹ� ������ ��ȸ
				orderList = db.executeQuery_ORDER();
				if (ServerUtils.isEmpty(orderList)) {
					ServerConst.MESSAGE_LOGGER.info("Order Message is not Exist");
				} else {
					System.out.println(orderList.size());
					ServerConst.MESSAGE_LOGGER.info("Order Message is Exist, msgNum : [{}]", orderList.size());
					for (OrderInfo orderNum : orderList) {
						orderNum.setOrder_list(db.executeQuery_ORDER_LIST(orderNum.getOrder_num()));
						//TODO �� �κ��� Ư�� ����ڿ��� �˸��� �����Ƿ� setPushPartial �ٲ���� 
					}
					//�ֹ����� ����Ʈ�� �ѱ�
					pushable.sendPushPartial(orderList);
				}
				
				// ��� ������ ��ȸ
				pushInfo = db.executeQuery_STOCK();
				if (ServerUtils.isEmpty(pushInfo)) {
					ServerConst.MESSAGE_LOGGER.debug("Push Message is not Exist");
				} else {
					ServerConst.MESSAGE_LOGGER.debug("Push Message is Exist");
					//��� ����ڿ��� ����
					pushable.sendPushAll(pushInfo);
				}
				
				
				// 5�� �������� �����尡 ����ȴ�.
				Thread.sleep(ServerConst.DB_THREAD_OBSERVER_TIME);
			} catch (InterruptedException e) {
				e.getStackTrace();
				ServerConst.MESSAGE_LOGGER.error(e.getMessage());
				try {
					db.closeDBSet();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					ServerConst.MESSAGE_LOGGER.error(e1.getMessage());
				}
			} finally {
				pushInfo = null;
				orderList.clear();
			}
		}
	}
	
}
