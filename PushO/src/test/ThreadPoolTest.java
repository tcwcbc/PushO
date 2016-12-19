package test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ThreadPoolTest {
	public static void main(String[] args) {
		new ThreadPoolTest();
	}
	
	public ThreadPoolTest() {
		ExecutorService executorService = Executors.newFixedThreadPool(5,
				new ThreadFactory() {
					
					@Override
					public Thread newThread(Runnable r) {
						// TODO Auto-generated method stub
						return (Thread)r;
					}
				});
		executorService.execute(new Handler());
	}
	
	class Handler implements Runnable{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			System.out.println(this.getClass()+"쓰레드 시작");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
