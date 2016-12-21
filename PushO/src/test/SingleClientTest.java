package test;

import java.io.IOException;

import client.OIOClient;
import client.res.ClientConst;

public class SingleClientTest {
	public static void main(String[] args) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OIOClient mcc = new OIOClient(Integer.parseInt(args[0]),"test1!");
				boolean flag = true;
				while (flag) {
					if (mcc.connectServer()) {
						try {
							mcc.processMsg();
						} catch (IOException e) {
							ClientConst.CLIENT_LOGGER.error("Time out ¹ß»ý...");
							continue;
						}
					}
				}
			}
		}).start();
	}
}
