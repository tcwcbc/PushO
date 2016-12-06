package model;

public class PushInfo {
	private String order_num;
	private String order_date;
	private String order_user;
	private String order_seller;
	private String order_price;
	private String order_list;
	
	public PushInfo(String order_num, String order_date, String order_user, String order_seller, String order_price) {
		this.order_num = order_num;
		this.order_date = order_date;
		this.order_user = order_user;
		this.order_seller = order_seller;
		this.order_price = order_price;
	}
	
	public void showInfo() {
		System.out.println("주문번호:" + order_num + "/" + "주문날짜:" + order_date + "/" + "구매자" + order_user + "/"
				+ "판매자:" + order_seller + "총 금액:" + order_price + "/" + "믈품리스트:" + order_list);
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

	public String getOrder_list() {
		return order_list;
	}

	public void setOrder_list(String order_list) {
		this.order_list = order_list;
	}
	
	
	
}
