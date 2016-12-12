package server.service;

/**
 * @author		최병철
 * @Description	푸시기능을 위한 인터페이스로 모두에게 보내는 푸시메소드와 특정 판매자에게 보내는 푸시메소드가 있다
 * TODO			특정 자료구조에 담은 연결들을 통하여 메시지를 발송하는 로직
 */
public interface Pushable {
	/**
	 * 모든 클라이언트에게 동일하게 메시지를 보내는 메소드
	 * @param msg	전송할 푸시 메시지
	 */
	public void sendPushAll(String msg);
	
	/**
	 * 특정 클라이언트에게 메시지를 보내는 메소드
	 * @param Id	클라이언트 아이디(Map<K,V>의 K값)
	 * @param msg	전송할 푸시 메시지
	 */
	public void sendPushPartial(String Id, String msg);
}
