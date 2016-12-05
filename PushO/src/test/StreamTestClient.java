package test;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.simple.JSONObject;

import res.Const;
import util.Utils;

public class StreamTestClient {
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		Socket socket = new Socket("localhost", 30000);
		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
		
		String msg1Str = Utils.makeJSONMessageForAuth("이름", "비밀번호",
				new JSONObject(), new JSONObject());
		byte[] msg1 = Utils.makeMessageStringToByte(
				new byte[Const.HEADER_LENTH+msg1Str.getBytes().length], msg1Str);
		
		String msg2Str = Utils.makeJSONMessageForAuth("이름입니다.", "비밀번호입니다.",
				new JSONObject(), new JSONObject());
		byte[] msg2 = Utils.makeMessageStringToByte(
				new byte[Const.HEADER_LENTH+msg2Str.getBytes().length], msg2Str);
		
		String msg3Str = Utils.makeJSONMessageForPingPong(new JSONObject(), true);
		byte[] msg3 = Utils.makeMessageStringToByte(
				new byte[Const.HEADER_LENTH+msg3Str.getBytes().length], msg3Str);
		
		
		System.out.println(msg1.length);
		System.out.println(new String(msg1));
		bos.write(msg1);
		
		System.out.println(msg2.length);
		System.out.println(new String(msg2));
		bos.write(msg2);
		
		System.out.println(msg3.length);
		System.out.println(new String(msg3));
		bos.write(msg3);
		
		
		Thread.sleep(1000);
		bos.close();
		socket.close();
	}
}
