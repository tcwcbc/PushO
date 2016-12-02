package server;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface SetPrepareStatement {
	public void setFields(PreparedStatement pstm) throws SQLException;
}
