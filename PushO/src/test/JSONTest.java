package test;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import util.Utils;

public class JSONTest {
	public static void main(String[] args) {
		jsonTest();
	}
	
	private static void jsonTest() {
		System.out.println(
				Utils.parseJSONMessage(new JSONParser(),
						Utils.makeJSONMessageForAuth("이름이", "패스워드", new JSONObject(), new JSONObject())
						)
				);
		System.out.println(
				Utils.parseJSONMessage(new JSONParser(),
						Utils.makeJSONMessageForPingPong(new JSONObject(),false))
				);
	}
}
