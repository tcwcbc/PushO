package server;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import dao.JDBCTemplate;

/**
 * @author		최병철
 * @Description	{@link JDBCTemplate}에서 쿼리를 동적으로 바꾸기 위한 콜백 인터페이스
 * TODO			람다로 변환 고려
 */
public interface SetPrepareStatement {
	public void setFields(PreparedStatement pstm) throws SQLException;
}
