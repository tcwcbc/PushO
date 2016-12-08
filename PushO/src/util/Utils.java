package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import model.ProductList;
import model.PushInfo;
import res.Const;

/**
 * @author �ֺ�ö
 * @Description ��� Ŭ�������� ���������� ���Ǵ� �޼ҵ���� ���� TODO File �б� �� ���⿡ ���� �κ� �����ʿ� ��Ƽ������
 *              ȯ�濡���� Ȱ���� ����Ͽ� ���ü� ���� �޽��� ������ ������ Fix���� �ʾ������� �޽����� ����� �Ľ��ϴ� �κ�
 *              ���� ����
 */
public class Utils {

	/**
	 * ������Ʈ�� ������� Ȯ���ϴ� �޼ҵ�
	 * 
	 * @param ������Ʈ
	 * @return ��������� true �����Ͱ� ������ false
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
				ret = ret + temp + Const.END_LINE;
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
	 * JSON�������� ������� msg�� byte[]�� ��ȯ�Ͽ� �� ���̸� ����� �ٿ� ret�� ��� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param ret
	 *            ��� ���� ��� ����Ʈ �迭, ���̴� 4(int�� ����Ʈ)+�޽��� ����Ʈ�� ���� ex)new
	 *            byte[](4+msg.getBytes().length);
	 * @param msg
	 *            JSON�������� ������� ����
	 * @return merge(byte[] header + byte[] body)
	 */
	public static byte[] makeMessageStringToByte(byte[] ret, String msg) {
		return mergeBytearrays(ret, intTobyte(msg.getBytes(Const.CHARSET).length), msg.getBytes(Const.CHARSET));
	}

	/**
	 * �̸��� ��й�ȣ�� ���� ������ ���� JSON�޽����� ����� �޼ҵ�
	 * 
	 * @param name
	 *            ����� �̸�
	 * @param passwd
	 *            ����� ��й�ȣ
	 * @param parent
	 *            �ֻ��� JSONObject
	 * @param childData
	 *            �������� data�� ��� �ִ� JSONObject
	 * @return JSON������ String data
	 */
	public static String makeJSONMessageForAuth(String name, String passwd, JSONObject parent, JSONObject childData) {
		// getLong
		parent.put(Const.JSON_KEY_SEND_TIME, getTime());
		// auth, pp, push getString
		parent.put(Const.JSON_KEY_DATA_CATEGORY, Const.JSON_VALUE_AUTH);
		// id, getString
		childData.put(Const.JSON_KEY_AUTH_ID, name);
		// passwd, getString
		childData.put(Const.JSON_KEY_AUTH_PASSWD, passwd);
		// data (JSONObject)getObjcet
		parent.put(Const.JSON_KEY_DATA, childData);

		return parent.toString();
	}

	/**
	 * �������� Ȯ���� ���� PingPong �޽����� ����� �޼ҵ�
	 * 
	 * @param isPing
	 *            true - Ping�����, false - Pong�����
	 * @return "ping"�̳� "pong"
	 */
	public static String makeJSONMessageForPingPong(JSONObject jsonObject, boolean isPing) {
		// getLong
		jsonObject.put(Const.JSON_KEY_SEND_TIME, getTime());
		// auth, pp, push getString
		if (isPing) {
			jsonObject.put(Const.JSON_KEY_DATA_CATEGORY, Const.JSON_VALUE_PING);
		} else {
			jsonObject.put(Const.JSON_KEY_DATA_CATEGORY, Const.JSON_VALUE_PONG);
		}
		return jsonObject.toString();
	}

	/**
	 * Ǫ�� �����͵��� Json�������� ��ȯ
	 * 
	 * @param data
	 *            �ֹ������� ��� ������ �迭
	 * @param parent
	 *            ���� Object
	 * @param childData
	 *            ���� Object
	 * @return JSON������ String data
	 */
	public static String makeJSONMessageForPush(PushInfo msg, JSONObject parent, JSONObject childData) {

		parent.put(Const.JSON_KEY_SEND_TIME, getTime());
		parent.put(Const.JSON_KEY_DATA_CATEGORY, Const.JSON_VALUE_PUSH);
		childData.put(Const.JSON_KEY_ORDER_NUM, msg.getOrder_num());
		childData.put(Const.JSON_KEY_ORDER_DATE, msg.getOrder_date());
		childData.put(Const.JSON_KEY_ORDER_USER, msg.getOrder_user());
		childData.put(Const.JSON_KEY_ORDER_SELLER, msg.getOrder_seller());
		childData.put(Const.JSON_KEY_ORDER_PRICE, msg.getOrder_price());

		JSONArray array = new JSONArray();

		for (int num = 0; num < msg.getOrder_list().size(); num++) {
			JSONObject jo = new JSONObject();
			jo.put(Const.JSON_KEY_ORDER_PRODUCT, msg.getOrder_list().get(num).getProduct());
			jo.put(Const.JSON_KEY_ORDER_PRODUCT_COUNT, msg.getOrder_list().get(num).getCount());
			array.add(jo);
		}
		childData.put(Const.JSON_KEY_ORDER_LIST, array);
		parent.put(Const.JSON_KEY_DATA, childData);

		return parent.toString();
	}

	/**
	 * String Ÿ������ ���� JSON���ڿ��� �Ľ��Ͽ� Ư�� �����͸� �����ϴ� �޼ҵ�
	 * 
	 * @param jsonParser
	 *            JSON �Ľ��� ���� Parser ��ü
	 * @param msg
	 *            String Ÿ���� JSON���ڿ�
	 * @return ���� �޽����� ī�װ��� auth�� ��� id��, ���� �޽����� ī�װ��� ping�� ��� pong, pong�� ���
	 *         ping�� ��ȯ
	 */
	public static String parseJSONMessage(JSONParser jsonParser, String msg) {
		String category = null;
		String result = null;
		try {
			JSONObject jsonObject = (JSONObject) jsonParser.parse(msg);
			category = (String) jsonObject.get(Const.JSON_KEY_DATA_CATEGORY);
			if (category.equals(Const.JSON_VALUE_PING)) {
				result = Const.JSON_VALUE_PING;
			} else if (category.equals(Const.JSON_VALUE_PONG)) {
				result = Const.JSON_VALUE_PONG;
			} else if (category.equals(Const.JSON_VALUE_AUTH)) {
				JSONObject object = (JSONObject) jsonObject.get(Const.JSON_KEY_DATA);
				String id = (String) object.get(Const.JSON_KEY_AUTH_ID);
				String passwd = (String) object.get(Const.JSON_KEY_AUTH_PASSWD);
				// result = id + "," + passwd;
				result = id;
			} else if (category.equals(Const.JSON_VALUE_PUSH)) {
				result = Const.JSON_VALUE_PUSH;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = "JSON �Ľ� ����";
		}
		return result;
	}

	public static PushInfo parsePushMessage(JSONParser jsonParser, String msg, PushInfo pushData) {
		try {
			JSONObject jsonObject = (JSONObject) jsonParser.parse(msg);
			JSONObject object = (JSONObject) jsonObject.get(Const.JSON_KEY_DATA);
			JSONArray array = (JSONArray) object.get(Const.JSON_KEY_ORDER_LIST);

			List<ProductList> pt = new ArrayList<>();
			for (int i = 0; i < array.size(); i++) {
				JSONObject order = (JSONObject) array.get(i);
				pt.add(new ProductList(order.get(Const.JSON_KEY_ORDER_PRODUCT).toString(),
						order.get(Const.JSON_KEY_ORDER_PRODUCT_COUNT).toString()));
			}

			pushData = new PushInfo(object.get(Const.JSON_KEY_ORDER_NUM).toString(),
					object.get(Const.JSON_KEY_ORDER_DATE).toString(), object.get(Const.JSON_KEY_ORDER_USER).toString(),
					object.get(Const.JSON_KEY_ORDER_SELLER).toString(),
					object.get(Const.JSON_KEY_ORDER_PRICE).toString(), pt);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("JSON �Ľ� ����");
		}
		return pushData;
	}

	/**
	 * int���� byte�迭�� �ٲ�
	 * 
	 * @param integer
	 *            ����Ʈ �迭�� ��ȯ�� ����
	 * 
	 *            order : ByteOrder.LITTLE_ENDIAN ByteOrder.BIG_ENDIAN
	 * @return ��� ����Ʈ �迭
	 */
	public static byte[] intTobyte(int integer) {
		ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE / 8);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		// �μ��� �Ѿ�� integer�� putInt�μ���
		buff.putInt(integer);
		return buff.array();
	}

	/**
	 * byte�迭�� int���� �ٲ�
	 * 
	 * @param bytes
	 *            ������ ��ȯ�� ����Ʈ �迭 order : ByteOrder.LITTLE_ENDIAN
	 *            ByteOrder.BIG_ENDIAN
	 * @return ��ȯ �� ����
	 */
	public static int byteToInt(byte[] bytes) {
		ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE / 8);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		// buff������� 4�� ������
		// bytes�� put�ϸ� position�� limit�� ���� ��ġ�� ��.
		buff.put(bytes);
		// flip()�� ���� �Ǹ� position�� 0�� ��ġ �ϰ� ��.
		buff.flip();
		return buff.getInt(); // position��ġ(0)���� ���� 4����Ʈ�� int�� �����Ͽ� ��ȯ
	}

	/**
	 * header ����Ʈ ���� body ����Ʈ �迭�� merge�Ͽ� ret�迭�� ��� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param ret
	 *            ��� ���� ��� ����Ʈ �迭, �� �迭�� ���̴� header�� ���̿� body�� ������ �� ex) new
	 *            byte[header.length+body.length];
	 * @param header
	 *            header ����Ʈ �迭
	 * @param body
	 *            body ����Ʈ �迭
	 * @return merge ����� ret ����Ʈ �迭�� ��� ��ȯ
	 */
	public static byte[] mergeBytearrays(byte[] ret, byte[] header, byte[] body) {
		System.arraycopy(header, 0, ret, 0, header.length);
		System.arraycopy(body, 0, ret, header.length, body.length);
		return ret;
	}

	/**
	 * ��ü ����Ʈ �迭���� header�� ������ body �κ��� �����Ͽ� ��ȯ�ϴ� �޼ҵ�
	 * 
	 * @param ret
	 *            ��� ���� ��� ����Ʈ �迭, �� �迭�� ���̴� ��ü ����Ʈ �迭���� ����� ���̸� �� �� ex) new
	 *            byte[bArr.length-headerLength];
	 * @param headerLength
	 *            header�� ����
	 * @param bArr
	 *            ��ü ����Ʈ �迭
	 * @return divide ����� ret ����Ʈ �迭�� ��� ��ȯ
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

}
