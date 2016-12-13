package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.TimerTask;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.res.ClientConst;
import client.util.ClientUtils;

public class ClientHeartBeat extends TimerTask {

	private BufferedOutputStream bos;

	public ClientHeartBeat(BufferedOutputStream bos) {
		this.bos = bos;
	}

	public void run() {
		try {
			String msgPingString = ClientUtils.makeJSONMessageForPingPong(new JSONObject(), true);
			byte[] msgPingByte = ClientUtils.makeMessageStringToByte(
					new byte[ClientConst.HEADER_LENTH + msgPingString.getBytes(ClientConst.CHARSET).length],
					msgPingString);
			bos.write(msgPingByte);
			bos.flush();
			System.out.println("heart beat Àü¼Û");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
