package test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log4jTest {
//	private final Logger logger = LogManager.getRootLogger();
	private final Logger clientLogger = LogManager.getLogger("client");
	private final Logger severLogger = LogManager.getLogger("server");
	public static void main(String[] args) {
		new Log4jTest();
	}
	
	public Log4jTest() {
		clientLogger.trace("클라이언트");
		clientLogger.error("클라이언트 에러");
		severLogger.trace("서버");
		severLogger.error("서버 에러");
	}
}
