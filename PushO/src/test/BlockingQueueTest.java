package test;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import server.res.ServerConst;

public class BlockingQueueTest {

	public static void main(String[] args) {

		LinkedBlockingQueue<String> receivedAckQueue = new LinkedBlockingQueue<String>(50);
		

		try {
			receivedAckQueue.put("2");
			receivedAckQueue.put("23");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		Iterator<String> iter = receivedAckQueue.iterator();
		
		while (iter.hasNext()) {
			receivedAckQueue.remove();
			String msg = iter.next();
			System.out.println(msg);
		}
		
		

		Iterator<String> iter2 = receivedAckQueue.iterator();

		while (iter2.hasNext()) {
			receivedAckQueue.remove();
			String msg = iter2.next();
			System.out.println(msg);
		}

		System.out.println("Å¥ ´Ù²¨³¿");
	}


}
