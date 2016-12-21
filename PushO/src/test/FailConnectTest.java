package test;

import java.io.IOException;

import client.OIOClient;
import client.res.ClientConst;

public class FailConnectTest {
	public static void main(String[] args) {
		new FailConnectTest();
	}
	public FailConnectTest() {
		unregisteredClient();
		alreadyConnected();
		passwordNotCorrect();
	}
	
	private void passwordNotCorrect() {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			@Override
			public void run() {
				OIOClient mcc = new OIOClient(21,"asd");
				boolean flag = true;
				while (flag) {
					if (mcc.connectServer()) {
						try {
							mcc.processMsg();
						} catch (IOException e) {
							ClientConst.CLIENT_LOGGER.error("Time out 발생...");
							continue;
						}
					}
				}
			}
		}).start();
	}
	private void alreadyConnected() {
		for(int i =0;i<2;i++){
			new Thread(new Runnable() {
				@Override
				public void run() {
					OIOClient mcc = new OIOClient(21,"test1!");
					boolean flag = true;
					while (flag) {
						if (mcc.connectServer()) {
							try {
								mcc.processMsg();
							} catch (IOException e) {
								ClientConst.CLIENT_LOGGER.error("Time out 발생...");
								continue;
							}
						}
					}
				}
			}).start();
		}
	}
	private void unregisteredClient() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OIOClient mcc = new OIOClient(1000,"test1!");
				boolean flag = true;
				while (flag) {
					if (mcc.connectServer()) {
						try {
							mcc.processMsg();
						} catch (IOException e) {
							ClientConst.CLIENT_LOGGER.error("Time out 발생...");
							continue;
						}
					}
				}
			}
		}).start();
	}
}
