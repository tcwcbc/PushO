package test;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import server.res.ServerConst;

public class BlockingQueueTest {

	public static void main (String[] args) {
		
		
		LinkedBlockingQueue<String> receivedAckQueue = new LinkedBlockingQueue<String>(50);
		
		receivedAckQueue.add("1");
		receivedAckQueue.add("2");
		receivedAckQueue.add("3");
		receivedAckQueue.add("4");
		receivedAckQueue.add("5");
		receivedAckQueue.add("6");
		
		Iterator<String> iter = receivedAckQueue.iterator();
		while(iter.hasNext()) {
			String msg = iter.next();
			System.out.println(msg);
		}
		
		System.out.println("Å¥ ´Ù²¨³¿");
	}

}
