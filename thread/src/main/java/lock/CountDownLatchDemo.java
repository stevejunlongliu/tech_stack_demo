package lock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CountDownLatchDemo {

    public static void test() throws InterruptedException {

        final CountDownLatch countDownLatchLock = new CountDownLatch(3);

        Runnable runnable = new Runnable() {
            public void run() {
                System.out.println(Thread.currentThread().getName() + ":running");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                countDownLatchLock.countDown();
            }
        };

        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(runnable);
            thread.start();
        }

        countDownLatchLock.await();
        //.countDownLatchLock.await(1000, TimeUnit.SECONDS);//超时放弃
        System.out.println("主程序继续");
    }

}
