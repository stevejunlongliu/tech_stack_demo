package lock;

import com.sun.xml.internal.ws.runtime.config.TubelineFeatureReader;

public class LockRunnableDemo implements Runnable {

    int num = 100;


    public static void test() {
        LockRunnableDemo lockRunnableDemo = new LockRunnableDemo();

        Thread a = new Thread(lockRunnableDemo, "a");
        Thread b = new Thread(lockRunnableDemo, "b");
        Thread c = new Thread(lockRunnableDemo, "c");
        a.start();
        b.start();
        c.start();
    }

    public void run() {

        synchronized (this.getClass()) {
            while (num > 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + ":" + num--);
            }
        }
    }
}
