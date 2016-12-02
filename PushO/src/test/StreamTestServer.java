package test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import util.Utils;

public class StreamTestServer {
	public static void main(String[] args) throws IOException, InterruptedException {
		ServerSocket serverSocket = new ServerSocket(30000);
		Socket socket =  serverSocket.accept();
		BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
		
		byte[] buf = new byte[4];
		int readCount=bis.read(buf);
		System.out.println(readCount);
		int length = Utils.byteToInt(buf);
		System.out.println(length);
		
		byte[] body = new byte[length];
		int bodylength = bis.read(body);
		System.out.println(bodylength);
		System.out.println(new String(body, 4, bodylength-4)
				);
		
		byte[] buf2 = new byte[4];
		int readCount2=bis.read(buf2);
		System.out.println(readCount2);
		int length2 = Utils.byteToInt(buf2);
		System.out.println(length2);
		
		byte[] body2 = new byte[length2];
		int bodylength2 = bis.read(body2);
		System.out.println(bodylength2);
		System.out.println(new String(body2, 4, bodylength2-4)
				);
		
		Thread.sleep(1000);
		bis.close();
		socket.close();
		serverSocket.close();
	}
}
