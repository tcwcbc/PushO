package model;

public class ProductList {
	private String product;
	private String count;
	
	public ProductList(String product, String count) {
		this.product = product;
		this.count = count;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}
	
	
}
