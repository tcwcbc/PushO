package test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log4jTest {
//	private final Logger logger = LogManager.getRootLogger();
	private final Logger logger = LogManager.getLogger("runMode");
	public static void main(String[] args) {
		new Log4jTest();
	}
	
	public Log4jTest() {
		logger.trace("����");
		logger.info("�����ٶ󸶹ٻ�");
		logger.trace("��");
	}
}
