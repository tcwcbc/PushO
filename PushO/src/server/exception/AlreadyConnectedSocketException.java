package server.exception;

/**
 * @author		최병철
 * @Description	이미 연결된 사용자일 경우 발생하는 예외
 */
public class AlreadyConnectedSocketException extends RuntimeException{
	public AlreadyConnectedSocketException(String msg) {
		super(msg);
	}
}
