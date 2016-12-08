package test;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import server.util.ServerUtils;

public class JSONTest {
	public static void main(String[] args) {
		jsonTest();
	}
	
	private static void jsonTest() {
		System.out.println(
				ServerUtils.parseJSONMessage(new JSONParser(),
						ServerUtils.makeJSONMessageForAuth("이름이", "패스워드", new JSONObject(), new JSONObject())
						)
				);
		System.out.println(
				ServerUtils.parseJSONMessage(new JSONParser(),
						ServerUtils.makeJSONMessageForPingPong(new JSONObject(),false))
				);
	}
}
