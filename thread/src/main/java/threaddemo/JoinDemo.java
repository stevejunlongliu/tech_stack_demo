package threaddemo;

public class JoinDemo implements Runnable {
    public void run() {
        try {
            for (int i = 0; i < 10; i++) {
                System.out.println(Thread.currentThread().getName() + ":" + i + " running");
                Thread.sleep(300);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void test() throws InterruptedException {

        Runnable runnable = new JoinDemo();
        Thread a = new Thread(runnable);
        a.setName("Athread");
        Thread b = new Thread(runnable);
        b.setName("Bthread");
        Thread c = new Thread(runnable);
        c.setName("Cthread");

        a.start();
        a.join();

        //当B线程执行到了A线程的.join（）方法时，B线程就会等待，等A线程都执行完毕，B线程才会执行。
        //join可以用来临时加入线程执行。
        b.start();
        c.start();
    }
}
