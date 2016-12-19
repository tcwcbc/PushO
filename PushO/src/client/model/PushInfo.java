package client.model;

import java.util.List;

/**
 * @author		김재우
 * @Description	재고알림 정보를 담을 Model
 * TODO			추후 변경될 수 있음
 */
public class PushInfo {
	
	private List<ProductList> order_list;

	public PushInfo(List<ProductList> order_list) {
		this.order_list = order_list;
	}	
	
	public List<ProductList> getOrder_list() {
		return order_list;
	}
	public void setOrder_list(List<ProductList> order_list) {
		this.order_list = order_list;
	}

	
}
