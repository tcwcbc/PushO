package test;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TimerTest {

	public static void main(String[] args) {
		Timer timer = new Timer();
		// 고정비율 (3초후 실행하며, 최초 실행 시간으로 부터 1초마다 실행함)
		timer.scheduleAtFixedRate(new HelloTask(), 3000, 1000);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		timer.cancel();

	}
}

class HelloTask extends TimerTask {
	public void run() {
		System.out.println(new Date());
	}
}
