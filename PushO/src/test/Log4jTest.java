package test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log4jTest {
	public static void main(String[] args) {
		new Log4jTest();
	}
	
	public Log4jTest() {
		Logger logger = LogManager.getLogger();
		
		logger.error("테스트로그");
	}
}
