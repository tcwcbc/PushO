package server.exception;

/**
 * @author		최병철
 * @Description	DB의 검색 결과가 없을 경우 발생하는 사용자 정의 예외
 */
public class EmptyResultDataException extends RuntimeException{
	public EmptyResultDataException(String msg) {
		super(msg);
	}
}
