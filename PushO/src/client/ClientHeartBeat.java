package client;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.TimerTask;
import org.json.simple.JSONObject;

import client.encry.AESUtils;
import client.res.ClientConst;
import client.util.ClientUtils;

public class ClientHeartBeat extends TimerTask {

	private BufferedOutputStream bos;
	private String aesKey;

	public ClientHeartBeat(BufferedOutputStream bos, String aesKey) {
		this.bos = bos;
		this.aesKey = aesKey;
	}

	public void run() {
		try {
			String msgPingString = ClientUtils.makeJSONMessageForPingPong(new JSONObject(), true);
			msgPingString = AESUtils.AES_Encode(msgPingString, aesKey);
			byte[] msgPingByte = ClientUtils.makeMessageStringToByte(
					new byte[ClientConst.HEADER_LENTH + msgPingString.getBytes(ClientConst.CHARSET).length],
					msgPingString);
			bos.write(msgPingByte);
			bos.flush();
			ClientConst.CLIENT_LOGGER.info("Send Ping Message");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
