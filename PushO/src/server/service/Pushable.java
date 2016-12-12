package server.service;

/**
 * @author		�ֺ�ö
 * @Description	Ǫ�ñ���� ���� �������̽��� ��ο��� ������ Ǫ�ø޼ҵ�� Ư�� �Ǹ��ڿ��� ������ Ǫ�ø޼ҵ尡 �ִ�
 * TODO			Ư�� �ڷᱸ���� ���� ������� ���Ͽ� �޽����� �߼��ϴ� ����
 */
public interface Pushable {
	/**
	 * ��� Ŭ���̾�Ʈ���� �����ϰ� �޽����� ������ �޼ҵ�
	 * @param msg	������ Ǫ�� �޽���
	 */
	public void sendPushAll(String msg);
	
	/**
	 * Ư�� Ŭ���̾�Ʈ���� �޽����� ������ �޼ҵ�
	 * @param Id	Ŭ���̾�Ʈ ���̵�(Map<K,V>�� K��)
	 * @param msg	������ Ǫ�� �޽���
	 */
	public void sendPushPartial(String Id, String msg);
}
