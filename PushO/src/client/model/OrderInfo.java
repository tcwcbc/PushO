package client.model;

import java.util.List;

/**
 * @author		�����
 * @Description	�ֹ����� �˸��� ���� Model
 * TODO			���� ����� �� ����
 */
public class OrderInfo {
	private String order_num;
	private String order_date;
	private String order_user;
	private String order_seller;
	private String order_price;
	private List<ProductList> order_list;
	private String result_info;
	
	
	public OrderInfo(String order_num, String order_date, String order_user, String order_seller, String order_price) {
		this.order_num = order_num;
		this.order_date = order_date;
		this.order_user = order_user;
		this.order_seller = order_seller;
		this.order_price = order_price;
	}
	
	public OrderInfo(String order_num, String order_date, String order_user, 
			String order_seller, String order_price, List<ProductList> order_list) {
		this.order_num = order_num;
		this.order_date = order_date;
		this.order_user = order_user;
		this.order_seller = order_seller;
		this.order_price = order_price;
		this.order_list = order_list;
	}
	
	public void showInfo() {
		System.out.println("�ֹ���ȣ:" + order_num + "/" + "�ֹ���¥:" + order_date + "/" + "������" + order_user + "/"
				+ "�Ǹ���:" + order_seller + "�� �ݾ�:" + order_price + "/" + "��ǰ����Ʈ:" + order_list.toString());
	}
	
	/**
	 * �ֹ������� ��ȯ�ϴ� �޼ҵ�
	 * @return �ֹ���ȣ/�ֹ���¥/������/�Ǹ���/�ѱݾ�/��ǰ����Ʈ
	 */
	public String getInfo() {
		result_info = order_num + "/" + order_date + "/" + order_user + "/" + order_seller + "/" + order_price + "/" + order_list;
		
		return result_info;
	}

	public String getOrder_num() {
		return order_num;
	}

	public void setOrder_num(String order_num) {
		this.order_num = order_num;
	}

	public String getOrder_date() {
		return order_date;
	}

	public void setOrder_date(String order_date) {
		this.order_date = order_date;
	}

	public String getOrder_user() {
		return order_user;
	}

	public void setOrder_user(String order_user) {
		this.order_user = order_user;
	}

	public String getOrder_seller() {
		return order_seller;
	}

	public void setOrder_seller(String order_seller) {
		this.order_seller = order_seller;
	}

	public String getOrder_price() {
		return order_price;
	}

	public void setOrder_price(String order_price) {
		this.order_price = order_price;
	}

	public List<ProductList> getOrder_list() {
		return order_list;
	}

	public void setOrder_list(List<ProductList> order_list) {
		this.order_list = order_list;
	}
}