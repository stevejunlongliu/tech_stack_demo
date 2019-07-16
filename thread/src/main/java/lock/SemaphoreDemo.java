package lock;

import java.util.concurrent.Semaphore;

public class SemaphoreDemo {


    public static void test() {

        final Semaphore semaphore = new Semaphore(3);

        Runnable runnable = new Runnable() {
            public void run() {
                boolean getlock = false;
                try {
                    //  semaphore.acquire();//如果获取不到许可，将会阻塞直到获取为止 //todo 如何确定阻塞数量最大值

                    if (semaphore.tryAcquire()) {
                    getlock = true;
                    System.out.println(Thread.currentThread().getName() + "进入semaphore");
                    System.out.println(Thread.currentThread().getName() + "执行");
                    Thread.sleep(3000);
                } else {
                    System.out.println(Thread.currentThread().getName() + "进入semaphore失败");
                }


            } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (getlock) {
                        semaphore.release();
                        System.out.println(Thread.currentThread().getName() + "释放");
                    }

                }
            }
        };

        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(runnable);
            thread.start();
        }

    }
}
