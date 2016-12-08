package server.observer;

import java.util.List;

/**
 * 
 * @author 김재우
 *
 */
public interface DBObserver {
	
	// DBThread 쓰레드에서 알림메시지 발송이 있을때 호출
	void msgPush(String msg);
	
	// AuthClientProxy에서 사용자 정보를 셋팅하면 OIOServer에서 HashMap에 사용자 정보를 담는다.
	void setUser(String id);
}
