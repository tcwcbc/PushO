package server.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import server.encry.AESUtils;
import server.exception.PushMessageSendingException;
import server.model.PushInfo;
import server.res.ServerConst;
import server.util.ServerUtils;

/**
 * @author 최병철
 * @Description 실제로 각 클라이언트와 소켓통신을 하며 작업을 수행하는 쓰레드
 * @TODO 입출력을 위한 스트림을 문자스트림->바이트스트림 으로 변환 수정된 유틸클래스 사용으로 메시지 작성방식 변경 타임아웃 예외가
 *       발생했을 경우 알림메시지를 전송하고 소켓해제 및 자원회수 매커니즘 예외처리들을 위한 많은 try-catch문을 정리
 */
public class ProcessCilentRequest extends Thread{

	private Socket connectedSocketWithClient;
	private byte[] msgPingByte;
	private byte[] msgPongByte;
	private byte[] msgPushByte;
	private boolean AuthSuccess = true;

	private BufferedOutputStream bos;
	private BufferedInputStream bis;
	
	private String aesKey;
	public LinkedBlockingQueue<PushInfo> receivedAckQueue;

	public ProcessCilentRequest(Socket socket, String aesKey, LinkedBlockingQueue<PushInfo> receivedAckQueue) {
		this.connectedSocketWithClient = socket;
		this.aesKey = aesKey;
		this.receivedAckQueue = receivedAckQueue;
		// 스트림에 대한 타임아웃 설정
		try {
			connectedSocketWithClient.setSoTimeout(ServerConst.STREAM_TIME_OUT);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ServerConst.SERVER_LOGGER.debug("ProcessClientRequest 생성");
	}

	@Override
	public void run() {
		try {
			bos = new BufferedOutputStream(connectedSocketWithClient.getOutputStream());
			bis = new BufferedInputStream(connectedSocketWithClient.getInputStream());

			/**
			 * 실제 데이터를 주고 받는 부분
			 * 
			 * @TODO : 핑퐁뿐만 아니라 실제 데이터를 주고받는 로직
			 */
			if (AuthSuccess) {
				sendSuccessMsg();
				AuthSuccess = false;
			}
		} catch (SocketTimeoutException timeoutE) {
			// TODO 타임아웃 발생 시에 자원회수 및 처리 매커니즘
			timeoutE.printStackTrace();
			try {
				bos.write(msgPingByte);
				System.out.println("Ping 전송");

			} catch (IOException e) {
				e.printStackTrace();
				try {
					bis.close();
					bos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void sendSuccessMsg() throws IOException, InterruptedException {
		String msgPingString = ServerUtils.makeJSONMessageForPingPong(new JSONObject(), false);
		msgPingString = AESUtils.AES_Encode(msgPingString, aesKey);
		msgPingByte = ServerUtils.makeMessageStringToByte(
				new byte[ServerConst.HEADER_LENTH + 
				         msgPingString.getBytes(ServerConst.CHARSET).length],
				msgPingString);

		byte[] header = new byte[ServerConst.HEADER_LENTH];
		byte[] body;
		int readCount = 0;
		int bodySize = 0;
		int bodylength = 0;
		bos.write(msgPingByte);
		bos.flush();
		ServerConst.SERVER_LOGGER.debug("인증성공 메시지 전송");

		while (!this.isInterrupted()) {
			readCount = bis.read(header);
			bodySize = ServerUtils.byteToInt(header);
			body = new byte[bodySize];
			bodylength = bis.read(body);

			String bodyDecodeMsg = AESUtils.AES_Decode(new String(body), aesKey);
			String msg = ServerUtils.parseJSONMessage(new JSONParser(), bodyDecodeMsg);

			if (msg.equals(ServerConst.JSON_VALUE_PONG)) {
				//TODO 주문내역발송에 대한 응답을 받을 경우 공유자원인 receivedAckQueue에 넣는 로직
				/*
				this.receivedAckQueue.put(new PushInfo());
				*/
				ServerConst.SERVER_LOGGER.debug("응답메시지 수신");
			}
			bos.flush();
		}
	}

	/**
	 * 클라이언트에게 알림을 전송하는 메소드 String 데이터를 구분자로 split하여 데이터 형식에 맞게 가공한다.
	 * @param msg		주문정보 메시지
	 * @throws PushMessageSendingException 보낼 때 스트림이 닫힌경우 연결이 해제되었음을 인지하고 풀과 맵에서 정리를 알리는 예외
	 */
	public void setPush(String msg) throws PushMessageSendingException {
		try {
			msg = AESUtils.AES_Encode(msg, aesKey);
			msgPushByte = ServerUtils.makeMessageStringToByte(
					new byte[ServerConst.HEADER_LENTH + msg.getBytes(ServerConst.CHARSET).length], msg);

			bos.write(msgPushByte);
			bos.flush();
			ServerConst.SERVER_LOGGER.info("푸시완료, "+this.getName());
		} catch (IOException e) {
			// 상대 클라이언트 접속이 끊어지면 발생
			// 그에 따라 HashMap에 저장되어있는 현재 Thread를 지우는 작업이 필요함
			this.interrupt();
			ServerConst.SERVER_LOGGER.error("푸시에러, "+e.getMessage());
			throw new PushMessageSendingException(e);
		}
	}
}
