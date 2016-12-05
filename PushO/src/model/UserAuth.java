package model;

/**
 * @author		최병철
 * @Description	테스트 테이블의 필드값들을 담을 Model
 * TODO			추후 인증에 대한 필드가 변경될 경우 수정
 */
public class UserAuth {
	private int id;
	private String name;
	private String ip;
	private int port;
	
	public UserAuth() {
	}
	
	public UserAuth(int id, String name, String ip, int port) {
		this.id = id;
		this.name = name;
		this.ip = ip;
		this.port = port;
	}

	@Override
	public String toString() {
		return "[ 유저 정보 : "+this.id+" , "+this.name+" , "+this.ip+" , "+this.port+" ]";
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
	
	
}
