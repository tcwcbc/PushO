package server;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import exception.EmptyResultDataException;
import observer.DBObserver;
import observer.DBThread;
import res.Const;

public class OIOServer implements DBObserver {
	
	public static void main(String[] args) {
		new OIOServer();
	}
	
	ArrayList<Socket> socketList = new ArrayList<Socket>();
	private ServerSocket serverSocket;
	private Socket socket;
	private DBThread dbThread;

	public OIOServer() {
		try {
			dbThread = new DBThread(this);
			dbThread.start();
			
			serverSocket = new ServerSocket(Const.PORT_NUM);
			System.out.println("서버시작...");
			AuthClientProxy authProxy = AuthClientProxy.getInstance();
			while (true) {
				socket = serverSocket.accept();
				socket.setSoTimeout(Const.STREAM_TIME_OUT);
				try {
					ProcessCilentRequest thread = authProxy.getClientSocketThread(socket);
					thread.start();
					socketList.add(socket);
					System.out.println("Client 연결처리 스레드 : " + thread.getId() + " , " + thread.getName());
				} catch (EmptyResultDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					socket.close();
					socketList.remove(socket);
					System.out.println("인증 실패, 소켓 닫힘");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("서버소켓 예외 : " + e.getMessage());
		} finally {
			try {
				socket.close();
				serverSocket.close();
				dbThread.obserberStop();
				socketList = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void msgPush(String msg) {
		System.out.println(msg);
	}
}
