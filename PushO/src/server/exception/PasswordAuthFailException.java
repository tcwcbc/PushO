package server.exception;


/**
 * 비밀번호 비교 후 일치하지 않을때 발생하는 사용자 정의 예외
 * @author user 김재우
 *
 */
public class PasswordAuthFailException extends RuntimeException {
	public PasswordAuthFailException(String msg) {
		super(msg);
	}
}
