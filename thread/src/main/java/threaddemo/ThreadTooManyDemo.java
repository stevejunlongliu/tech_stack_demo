package threaddemo;

public class ThreadTooManyDemo implements Runnable {

    public void run() {
        try {
            System.out.println(System.currentTimeMillis() + ":" + Thread.currentThread().getName() + "开始执行");
            Thread.sleep(10000);
            System.out.println(System.currentTimeMillis() + ":" + Thread.currentThread().getName() + "结束");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void test() {
        //cpu只有4核心，试试同时进行20线程时，cpu是否会放出时间片给剩余的16线程执行过程
        for (int i = 0; i < 10; i++) {
            Thread A = new Thread(new ThreadTooManyDemo());

            A.start();
        }
    }
}
