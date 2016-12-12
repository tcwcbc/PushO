package server.exception;
/**
 * @author		최병철
 * @Description	푸시메시지를 보내는 도중 발생하는 예외를 정의한 클래스	
 * TODO			
 */
public class PushMessageSendingException extends RuntimeException{
	public PushMessageSendingException(Throwable e) {
		super(e);
	}
}
