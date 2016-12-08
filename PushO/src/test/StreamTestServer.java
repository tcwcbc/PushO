package test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import server.res.ServerConst;
import server.util.ServerUtils;

public class StreamTestServer {
	public static void main(String[] args)  {
		ServerSocket serverSocket = null;
		Socket socket = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			serverSocket = new ServerSocket(9999);
		
		socket =  serverSocket.accept();
		bis = new BufferedInputStream(socket.getInputStream());
//		bos = new BufferedOutputStream(socket.getOutputStream());
		byte[] buf = new byte[ServerConst.HEADER_LENTH];
		int readCount = 0;
		int length = 0;
		int bodylength = 0;
		
		while((readCount=bis.read(buf))!=-1){
			length = ServerUtils.byteToInt(buf);
			byte[] body = new byte[length];
			bodylength = bis.read(body);
			
			System.out.println(readCount);
			System.out.println(length);
			System.out.println(bodylength);
			System.out.println(new String(body));
			System.out.println(ServerUtils.parseJSONMessage(new JSONParser(), new String(body)));
		}
		
		String msgPingString = ServerUtils.makeJSONMessageForPingPong(new JSONObject(), true);
		byte[] msgPingByte = ServerUtils.makeMessageStringToByte(
				new byte[ServerConst.HEADER_LENTH+msgPingString.getBytes().length], msgPingString);
		String msgPongString = ServerUtils.makeJSONMessageForPingPong(new JSONObject(), false);
		byte[] msgPongByte = ServerUtils.makeMessageStringToByte(
				new byte[ServerConst.HEADER_LENTH+msgPongString.getBytes().length], msgPongString);
		
		/*while((readCount=bis.read(buf))!=-1){
			length = Utils.byteToInt(buf);
			byte[] body = new byte[length];
			bodylength = bis.read(body);
			String pp = Utils.parseJSONMessage(new JSONParser(), new String(body));
			if(pp.equals(Const.JSON_VALUE_PING)){
				bos.write(msgPongByte);
				System.out.println("Pong 전송");
			}
			if(pp.equals(Const.JSON_VALUE_PONG)){
				bos.write(msgPingByte);
				System.out.println("Ping 전송");
			}
		}*/
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				bis.close();
				socket.close();
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
