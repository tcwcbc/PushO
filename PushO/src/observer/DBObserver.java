package observer;

import java.util.List;

/**
 * 
 * @author �����
 *
 */
public interface DBObserver {
	
	// DBThread �����忡�� �˸��޽��� �߼��� ������ ȣ��
	void msgPush(String msg);
	
	// AuthClientProxy���� ����� ������ �����ϸ� OIOServer���� HashMap�� ����� ������ ��´�.
	void setUser(String id);
}
