package test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.simple.parser.JSONParser;

import res.Const;
import util.Utils;

public class StreamTestServer {
	public static void main(String[] args)  {
		ServerSocket serverSocket = null;
		Socket socket = null;
		BufferedInputStream bis = null;
		try {
			serverSocket = new ServerSocket(30000);
		
		socket =  serverSocket.accept();
		bis = new BufferedInputStream(socket.getInputStream());
		
		byte[] buf = new byte[Const.HEADER_LENTH];
		int readCount = 0;
		int length = 0;
		int bodylength = 0;
		
		while((readCount=bis.read(buf))!=-1){
			length = Utils.byteToInt(buf);
			byte[] body = new byte[length];
			bodylength = bis.read(body);
			
			System.out.println(readCount);
			System.out.println(length);
			System.out.println(bodylength);
			System.out.println(new String(body));
			System.out.println(Utils.parseJSONMessage(new JSONParser(), new String(body)));
		}
		
		
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
