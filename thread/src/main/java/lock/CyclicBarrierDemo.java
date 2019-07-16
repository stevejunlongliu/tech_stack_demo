package lock;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CyclicBarrierDemo {

    public static void test() {
        final CyclicBarrier cyclicBarrier = new CyclicBarrier(3);

        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + "等待中");
                try {
                    cyclicBarrier.await(1000, TimeUnit.SECONDS);//超时放弃
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + ":完成");
            }
        };

        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(runnable);
            thread.start();
        }

        System.out.println("主程序继续");
    }
}
