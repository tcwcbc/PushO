package server.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;

import org.json.simple.parser.JSONParser;

import server.dao.JDBCTemplate;
import server.encry.AESUtils;
import server.encry.KeyExchangeServer;
import server.exception.AlreadyConnectedSocketException;
import server.exception.EmptyResultDataException;
import server.res.ServerConst;
import server.util.ServerUtils;

/**
 * @author 최병철
 * @Description 인증을 위한 프록시 클래스로 싱글톤으로 구현 됨 
 * @TODO 싱글톤으로 구현시 멀티쓰레드 환경에서의 동시성 문제 제고
 *              인증을 위한 DB입출력 Blocking 시간 고려
 */
public class AuthClientHandler extends Thread {
	private SocketConnectionManager socketConnectionManagerager = SocketConnectionManager.getInstance();
	
	/*
	private static AuthClientHandler instance = null;
	
	public static AuthClientHandler getInstance() {
		if (instance == null) {
			instance = new AuthClientHandler();
			ServerConst.SERVER_LOGGER.debug("핸들러 생성");
		}
		return instance;
	}*/
	
	public ArrayBlockingQueue<Socket> socketQueue;
	
	public AuthClientHandler(ArrayBlockingQueue<Socket> socketQueue) {
		this.socketQueue = socketQueue;
		ServerConst.ACCESS_LOGGER.debug("AuthHandler Created!");
	}

	@Override
	public void run() {
		ServerConst.ACCESS_LOGGER.debug("AuthHandler Thread Start!");
		while(!this.isInterrupted()){
			try {
				Socket socket = this.socketQueue.take();
				ServerConst.ACCESS_LOGGER.info("Get Element from BlockingQueue for Authorization, BlockingQueue Size : {}",this.socketQueue.size());
				String aesKey = encryptionKeyChange(socket);
				ServerConst.ACCESS_LOGGER.info("Complete Key Exchage with [{}]",socket.getInetAddress().getHostName());
				ServerConst.ACCESS_LOGGER.info("Encryption Key : [{}] ", aesKey);
				authClientAndDelegate(socket, aesKey);
				ServerConst.ACCESS_LOGGER.debug("Complete Authorization!");
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ServerConst.ACCESS_LOGGER.error(e.getMessage());
			}
		}
	}


	private String encryptionKeyChange(Socket socket) {
		KeyExchangeServer kes = new KeyExchangeServer(socket);
		String key = kes.start();
		return key;
	}


	/**
	 * 실제로 인증을 수행하는 메소드
	 * 
	 * @param socket
	 *            인증 메시지를 위한 Stream을 얻을 목적의 socket
	 * @return 클라이언트 요청처리 쓰레드
	 * @throws EmptyResultDataException
	 *             등록된 사용자가 아님(인증X)
	 */
	public synchronized void authClientAndDelegate(Socket socket, String aesKey){
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());

			byte[] buf = new byte[ServerConst.HEADER_LENTH];
			int readCount = 0;
			int length = 0;
			int bodylength = 0;

			readCount = bis.read(buf);
			length = ServerUtils.byteToInt(buf);
			byte[] body = new byte[length];
			bodylength = bis.read(body);
			String msg = new String(body, ServerConst.CHARSET);
			msg = AESUtils.AES_Decode(msg, aesKey);
			ServerConst.ACCESS_LOGGER.info("Received Message from Client : [{}]",msg);

			if (msg.contains(ServerConst.JSON_VALUE_AUTH)) {
				String name = ServerUtils.parseJSONMessage(new JSONParser(), msg);
				boolean authorized = false;
				try{
					//인증 실패 시 예외가 발생되는 부분
					checkAuthorization(name);
					authorized = true;
					ServerConst.ACCESS_LOGGER.info("Client [{}] in Database",name);
					if(authorized){
						////매니저에 추가해주는 부분.
						ServerConst.ACCESS_LOGGER.info("Delegate to Manager, Name:[{}], AESKey:[{}]", name, aesKey);
						socketConnectionManagerager.addClientSocket(name, socket, aesKey);
					}
				} catch(EmptyResultDataException e){
					//TODO 클라이언트에게 인증되지 않았다는 메시지를 보냄
//					bos.write(b);
					ServerConst.ACCESS_LOGGER.error("Not Registered Client ERROR : {}",e.getMessage());
					socket.close();
					e.printStackTrace();
				} catch(AlreadyConnectedSocketException e){ 
					//TODO 클라이언트에게 이미 연결된 아이디라는 메시지를 보냄
//					bos.write(b);
					ServerConst.ACCESS_LOGGER.error("Aleady Connected Client ERROR : {} ",e.getMessage());
					socket.close();
					e.printStackTrace();
				}
				
				
			}
		} catch (IOException e) {
			e.printStackTrace();
			ServerConst.ACCESS_LOGGER.error(e.getMessage());
			try {
				bis.close();
			} catch (IOException closeE) {
				closeE.printStackTrace();
				ServerConst.ACCESS_LOGGER.error(e.getMessage());
			}
		}
	}

	/**
	 * {@link JDBCTemplate}을 활용한 사용자 인증
	 * 
	 * @param name
	 *            인증을 위한 사용자 이름
	 * @throws EmptyResultDataException
	 *             인증이 안되었을 경우 발생
	 */
	private void checkAuthorization(String name) throws EmptyResultDataException {
		new JDBCTemplate().executeQuery("select * from pj_member where mem_name = ?", 
				new SetPrepareStatement() {
			@Override
			public void setFields(PreparedStatement pstm) throws SQLException {
				ServerConst.ACCESS_LOGGER.debug("Access DataBase...");
				pstm.setString(1, name);
			}
		});
	}
}
