package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

import res.Const;
import util.Utils;

public class OIOClient {
	
	public static void main(String[] args) {
		new OIOClient();
	}

	private BufferedWriter bw;
	private Socket socket;
	private BufferedReader br;

	public OIOClient() {
		try {
			socket = new Socket(Const.SERVER_IP, Const.PORT_NUM);
			socket.setSoTimeout(Const.STREAM_TIME_OUT);
			bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			bw.write(Utils.makeJSONMessageForAuth("가나다", "1111"));
			System.out.println("인증 보냄");
			// bw.write(Utils.readFile(Const.SRC_FILE_PATH+Const.SRC_FILE_NAME));
			bw.flush();
			
			String text = null;
			while((text = br.readLine())!=null){
				Thread.sleep(Const.SEND_WATING_TIME);
				String pp = Utils.parseJSONMessage(text);
				if(pp.equals(Const.JSON_VALUE_PING)){
					bw.write(Utils.makeJSONMessageForPingPong(false));
					System.out.println("Pong 전송");
				}
				if(pp.equals(Const.JSON_VALUE_PONG)){
					bw.write(Utils.makeJSONMessageForPingPong(true));
					System.out.println("Ping 전송");
				}
				bw.flush();
			}
			
//			autoPingPong(3);
//			text = br.readLine();
//			autoPingPong(3);
			
		}catch (SocketTimeoutException timeoutE){
			timeoutE.printStackTrace();
			try {
				bw.write(Utils.makeJSONMessageForPingPong(true));
				System.out.println("Ping 전송");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					br.close();
					bw.close();
					socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void autoPingPong(int time) throws IOException, InterruptedException {
		String text;
		for(int i = 0; i<time; i++){
			text = br.readLine();
			Thread.sleep(Const.SEND_WATING_TIME);
			String pp = Utils.parseJSONMessage(text);
			if(pp.equals(Const.JSON_VALUE_PING)){
				bw.write(Utils.makeJSONMessageForPingPong(false));
				System.out.println("Pong 전송");
			}
			if(pp.equals(Const.JSON_VALUE_PONG)){
				bw.write(Utils.makeJSONMessageForPingPong(true));
				System.out.println("Ping 전송");
			}
			bw.flush();
		}
	}
}
