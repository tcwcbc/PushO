package test;

import util.Utils;

public class JSONTest {
	public static void main(String[] args) {
		jsonTest();
	}
	
	private static void jsonTest() {
		System.out.println(
				Utils.parseJSONMessage(
						Utils.makeJSONMessageForAuth(
								"테스트아이디", "테스트 비밀번호"))
				);
		System.out.println(
				Utils.parseJSONMessage(
						Utils.makeJSONMessageForPingPong(
								true))
				);
	}
}
