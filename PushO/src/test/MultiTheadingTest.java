package test;

import java.io.IOException;

import client.OIOClient;
import client.res.ClientConst;

public class MultiTheadingTest {
	public static void main(String[] args) {
		new MultiTheadingTest();
	}

	int i=0;
	public MultiTheadingTest() {
		for (i = 0; i < 12; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					OIOClient mcc = new OIOClient(i);
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
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
