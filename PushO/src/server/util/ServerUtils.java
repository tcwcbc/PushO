package server.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import client.res.ClientConst;
import server.model.ProductList;
import server.model.PushInfo;
import server.model.OrderInfo;
import server.res.ServerConst;

/**
 * @author 최병철
 * @Description 모든 클래스에서 공통적으로 사용되는 메소드들을 정의 TODO File 읽기 및 쓰기에 대한 부분 수정필요 멀티쓰레드
 *              환경에서의 활용을 고려하여 동시성 제고 메시지 전문의 형식이 Fix되지 않았음으로 메시지를 만들고 파싱하는 부분
 *              수정 가능
 */
public class ServerUtils {

	/**
	 * 오브젝트가 비었는지 확인하는 메소드
	 * 
	 * @param 오브젝트
	 * @return 비어있으면 true 데이터가 있으면 false
	 */
	public static boolean isEmpty(Object s) {
		if (s == null) {
			return true;
		}
		if ((s instanceof String) && (((String) s).trim().length() == 0)) {
			return true;
		}
		if (s instanceof Map) {
			return ((Map<?, ?>) s).isEmpty();
		}
		if (s instanceof List) {
			return ((List<?>) s).isEmpty();
		}
		if (s instanceof Object[]) {
			return (((Object[]) s).length == 0);
		}
		return false;
	}

	public static String readFile(String filePath) {
		String ret = "";
		String temp = "";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filePath));
			while ((temp = br.readLine()) != null) {
				ret = ret + temp + ServerConst.END_LINE;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

			try {
				br.close();
				br = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ret;
	}

	public static void writeFile(String filePath, String text) {

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(filePath, true));
			bw.write(text);
			bw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bw = null;
		}
	}

	/**
	 * JSON형식으로 만들어진 msg를 byte[]로 변환하여 그 길이를 헤더에 붙여 ret에 담아 반환하는 메소드
	 * 
	 * @param ret
	 *            결과 값이 담길 바이트 배열, 길이는 4(int의 바이트)+메시지 바이트의 길이 ex)new
	 *            byte[](4+msg.getBytes().length);
	 * @param msg
	 *            JSON형식으로 만들어진 본문
	 * @return merge(byte[] header + byte[] body)
	 */
	public static byte[] makeMessageStringToByte(byte[] ret, String msg) {
		return mergeBytearrays(ret, intTobyte(msg.getBytes(ServerConst.CHARSET).length),
				msg.getBytes(ServerConst.CHARSET));
	}

	/**
	 * 이름과 비밀번호를 통해 인증을 위한 JSON메시지를 만드는 메소드
	 * 
	 * @param name
	 *            사용자 이름
	 * @param passwd
	 *            사용자 비밀번호
	 * @param parent
	 *            최상위 JSONObject
	 * @param childData
	 *            실질적인 data를 담고 있는 JSONObject
	 * @return JSON형식의 String data
	 */
	public static String makeJSONMessageForAuth(String name, String passwd, JSONObject parent, JSONObject childData) {
		// getLong
		parent.put(ServerConst.JSON_KEY_SEND_TIME, getTime());
		// auth, pp, push getString
		parent.put(ServerConst.JSON_KEY_DATA_CATEGORY, ServerConst.JSON_VALUE_AUTH);
		// id, getString
		childData.put(ServerConst.JSON_KEY_AUTH_ID, name);
		// passwd, getString
		childData.put(ServerConst.JSON_KEY_AUTH_PASSWD, passwd);
		// data (JSONObject)getObjcet
		parent.put(ServerConst.JSON_KEY_DATA, childData);

		return parent.toString();
	}

	/**
	 * 연결유지 확인을 위한 PingPong 메시지를 만드는 메소드
	 * 
	 * @param isPing
	 *            true - Ping만들기, false - Pong만들기
	 * @return "ping"이나 "pong"
	 */
	public static String makeJSONMessageForPingPong(JSONObject jsonObject, boolean isPing) {
		// getLong
		jsonObject.put(ServerConst.JSON_KEY_SEND_TIME, getTime());
		// auth, pp, push getString
		if (isPing) {
			jsonObject.put(ServerConst.JSON_KEY_DATA_CATEGORY, ServerConst.JSON_VALUE_PING);
		} else {
			jsonObject.put(ServerConst.JSON_KEY_DATA_CATEGORY, ServerConst.JSON_VALUE_PONG);
		}
		return jsonObject.toString();
	}

	/**
	 * 푸시 데이터들을 Json포멧으로 변환
	 * 
	 * @param data
	 *            주문정보가 담긴 데이터 배열
	 * @param parent
	 *            상위 Object
	 * @param childData
	 *            하위 Object
	 * @return JSON형식의 String data
	 */
	public static String makeJSONMessageForPush(OrderInfo msg, JSONObject parent, JSONObject childData) {

		parent.put(ServerConst.JSON_KEY_SEND_TIME, getTime());
		parent.put(ServerConst.JSON_KEY_DATA_CATEGORY, ServerConst.JSON_VALUE_PUSH_ORDER);
		childData.put(ServerConst.JSON_KEY_ORDER_NUM, msg.getOrder_num());
		childData.put(ServerConst.JSON_KEY_ORDER_DATE, msg.getOrder_date());
		childData.put(ServerConst.JSON_KEY_ORDER_USER, msg.getOrder_user());
		childData.put(ServerConst.JSON_KEY_ORDER_SELLER, msg.getOrder_seller());
		childData.put(ServerConst.JSON_KEY_ORDER_PRICE, msg.getOrder_price());

		JSONArray array = new JSONArray();

		for (int num = 0; num < msg.getOrder_list().size(); num++) {
			JSONObject jo = new JSONObject();
			jo.put(ServerConst.JSON_KEY_ORDER_PRODUCT, msg.getOrder_list().get(num).getProduct());
			jo.put(ServerConst.JSON_KEY_ORDER_PRODUCT_COUNT, msg.getOrder_list().get(num).getCount());
			array.add(jo);
		}
		childData.put(ServerConst.JSON_KEY_ORDER_LIST, array);
		parent.put(ServerConst.JSON_KEY_DATA, childData);

		return parent.toString();
	}
	
	public static String makeJSONMessageForPushAll(PushInfo msg, JSONObject parent, JSONObject childData) {

		parent.put(ServerConst.JSON_KEY_SEND_TIME, getTime());
		parent.put(ServerConst.JSON_KEY_DATA_CATEGORY, ServerConst.JSON_VALUE_PUSH_STOCK);
	
		JSONArray array = new JSONArray();

		for (int num = 0; num < msg.getOrder_list().size(); num++) {
			JSONObject jo = new JSONObject();
			jo.put(ServerConst.JSON_KEY_ORDER_PRODUCT, msg.getOrder_list().get(num).getProduct());
			jo.put(ServerConst.JSON_KEY_ORDER_PRODUCT_COUNT, msg.getOrder_list().get(num).getCount());
			array.add(jo);
		}
		childData.put(ServerConst.JSON_KEY_ORDER_LIST, array);
		parent.put(ServerConst.JSON_KEY_DATA, childData);

		return parent.toString();
	}

	public static String makeJSONMessageForEncry(String key, JSONObject parent, JSONObject childData) {

		parent.put(ClientConst.JSON_KEY_SEND_TIME, getTime());
		parent.put(ClientConst.JSON_KEY_DATA_CATEGORY, ClientConst.JSON_VALUE_ENCRY);
		childData.put(ClientConst.JSON_KEY_AUTH_ENCRY, key);
		parent.put(ClientConst.JSON_KEY_DATA, childData);

		return parent.toString();
	}
	
	/**
	 * 키교환을 위한 파서
	 * @param jsonParser
	 * @param msg
	 * @return
	 */
	public static String parseEncryMessage(JSONParser jsonParser, String msg) {
		String key = null;
		try {
			JSONObject jsonObject = (JSONObject) jsonParser.parse(msg);
			JSONObject object = (JSONObject) jsonObject.get(ClientConst.JSON_KEY_DATA);

			key = (String) object.get(ClientConst.JSON_KEY_AUTH_ENCRY);
		} catch (ParseException e) {
			e.printStackTrace();
			key = "JSON 파싱 에러";
		}

		return key;
	}

	/**
	 * String 타입으로 받은 JSON문자열을 파싱하여 특정 데이터를 추출하는 메소드
	 * 
	 * @param jsonParser
	 *            JSON 파싱을 위한 Parser 객체
	 * @param msg
	 *            String 타입의 JSON문자열
	 * @return 받은 메시지의 카테고리가 auth일 경우 id값, 받은 메시지의 카테고리가 ping일 경우 pong, pong일 경우
	 *         ping을 반환
	 */
	public static String parseJSONMessage(JSONParser jsonParser, String msg) {
		String category = null;
		String result = null;
		try {
			JSONObject jsonObject = (JSONObject) jsonParser.parse(msg);
			category = (String) jsonObject.get(ServerConst.JSON_KEY_DATA_CATEGORY);
			if (category.equals(ServerConst.JSON_VALUE_PING)) {
				result = ServerConst.JSON_VALUE_PING;
			} else if (category.equals(ServerConst.JSON_VALUE_PONG)) {
				result = ServerConst.JSON_VALUE_PONG;
			} else if (category.equals(ServerConst.JSON_VALUE_AUTH)) {
				JSONObject object = (JSONObject) jsonObject.get(ServerConst.JSON_KEY_DATA);
				String id = (String) object.get(ServerConst.JSON_KEY_AUTH_ID);
				String passwd = (String) object.get(ServerConst.JSON_KEY_AUTH_PASSWD);
				result = id + "/" + passwd;
				//result = id;
			} else if (category.equals(ServerConst.JSON_VALUE_PUSH_ORDER)) {
				JSONObject object = (JSONObject) jsonObject.get(ServerConst.JSON_KEY_DATA);
				String response = (String) object.get(ServerConst.JSON_KEY_ORDER_RESPONSE);
				result = ServerConst.JSON_VALUE_PUSH_ORDER + "/" + response;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = "JSON 파싱 에러";
		}
		return result;
	}

	public static OrderInfo parsePushMessage(JSONParser jsonParser, String msg, OrderInfo pushData) {
		try {
			JSONObject jsonObject = (JSONObject) jsonParser.parse(msg);
			JSONObject object = (JSONObject) jsonObject.get(ServerConst.JSON_KEY_DATA);
			JSONArray array = (JSONArray) object.get(ServerConst.JSON_KEY_ORDER_LIST);

			List<ProductList> pt = new ArrayList<>();
			for (int i = 0; i < array.size(); i++) {
				JSONObject order = (JSONObject) array.get(i);
				pt.add(new ProductList(order.get(ServerConst.JSON_KEY_ORDER_PRODUCT).toString(),
						order.get(ServerConst.JSON_KEY_ORDER_PRODUCT_COUNT).toString()));
			}

			pushData = new OrderInfo(object.get(ServerConst.JSON_KEY_ORDER_NUM).toString(),
					object.get(ServerConst.JSON_KEY_ORDER_DATE).toString(),
					object.get(ServerConst.JSON_KEY_ORDER_USER).toString(),
					object.get(ServerConst.JSON_KEY_ORDER_SELLER).toString(),
					object.get(ServerConst.JSON_KEY_ORDER_PRICE).toString(), pt);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("JSON 파싱 에러");
		}
		return pushData;
	}

	/**
	 * int형을 byte배열로 바꿈
	 * 
	 * @param integer
	 *            바이트 배열로 변환할 정수
	 * 
	 *            order : ByteOrder.LITTLE_ENDIAN ByteOrder.BIG_ENDIAN
	 * @return 결과 바이트 배열
	 */
	public static byte[] intTobyte(int integer) {
		ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE / 8);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		// 인수로 넘어온 integer을 putInt로설정
		buff.putInt(integer);
		return buff.array();
	}

	/**
	 * byte배열을 int형로 바꿈
	 * 
	 * @param bytes
	 *            정수로 변환할 바이트 배열 order : ByteOrder.LITTLE_ENDIAN
	 *            ByteOrder.BIG_ENDIAN
	 * @return 변환 된 정수
	 */
	public static int byteToInt(byte[] bytes) {
		ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE / 8);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		// buff사이즈는 4인 상태임
		// bytes를 put하면 position과 limit는 같은 위치가 됨.
		buff.put(bytes);
		// flip()가 실행 되면 position은 0에 위치 하게 됨.
		buff.flip();
		return buff.getInt(); // position위치(0)에서 부터 4바이트를 int로 변경하여 반환
	}

	/**
	 * header 바이트 열과 body 바이트 배열을 merge하여 ret배열에 담아 반환하는 메소드
	 * 
	 * @param ret
	 *            결과 값이 담길 바이트 배열, 이 배열의 길이는 header의 길이와 body의 길이의 합 ex) new
	 *            byte[header.length+body.length];
	 * @param header
	 *            header 바이트 배열
	 * @param body
	 *            body 바이트 배열
	 * @return merge 결과를 ret 바이트 배열에 담아 반환
	 */
	public static byte[] mergeBytearrays(byte[] ret, byte[] header, byte[] body) {
		System.arraycopy(header, 0, ret, 0, header.length);
		System.arraycopy(body, 0, ret, header.length, body.length);
		return ret;
	}

	/**
	 * 전체 바이트 배열에서 header를 제외한 body 부분을 추출하여 반환하는 메소드
	 * 
	 * @param ret
	 *            결과 값이 담길 바이트 배열, 이 배열의 길이는 전체 바이트 배열에서 헤더의 길이를 뺀 값 ex) new
	 *            byte[bArr.length-headerLength];
	 * @param headerLength
	 *            header의 길이
	 * @param bArr
	 *            전체 바이트 배열
	 * @return divide 결과를 ret 바이트 배열에 담아 반환
	 */
	public static byte[] divideBytearrays(byte[] ret, int headerLength, byte[] bArr) {
		System.arraycopy(bArr, headerLength, ret, 0, bArr.length - headerLength);
		return ret;
	}

	public static String getTime() {
		long time = System.currentTimeMillis();
		SimpleDateFormat dayTime = new SimpleDateFormat("yyyyMMddhhmmss");
		String str = dayTime.format(new Date(time));

		return str;
	}

	public static String getEncryptValue(String pwd) {
		String value = null;
		
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(pwd.getBytes());

			byte byteData[] = md.digest();

			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
			String retVal = sb.toString();
			System.out.println(retVal);
		} catch (NoSuchAlgorithmException e) {
			//Sha256 해싱 오류
			e.printStackTrace();
		}

		return value;
	}
}
