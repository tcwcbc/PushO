package model;

public class UserAuth {
	private int id;
	private String name;
	private String ip;
	private int port;
	
	public UserAuth(int id, String name, String ip, int port) {
		// TODO Auto-generated constructor stub
		this.id = id;
		this.name = name;
		this.ip = ip;
		this.port = port;
	}

	public UserAuth() {
		// TODO Auto-generated constructor stub
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "[ 유저 정보 : "+this.id+" , "+this.name+" , "+this.ip+" , "+this.port+" ]";
	}
	
}
