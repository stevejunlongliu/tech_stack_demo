package threaddemo;

//实现runnable接口
public class RunableDemo implements Runnable {


    public void run() {

        for (int i = 0; i < 100; i++) {
            System.out.println(Thread.currentThread().getName() + "：正在执行！" + i);
        }
    }

    public static void test() throws InterruptedException {
        RunableDemo runableDemo = new RunableDemo();

        Thread aThread = new Thread(runableDemo);
        aThread.setName("线程A");

        Thread bThread = new Thread(runableDemo);
        bThread.setName("线程B");

        aThread.start();
        bThread.start();

      //ss  Thread.sleep(200);
        for (int i = 0; i < 100; i++) {
            System.out.println("main线程：正在执行！" + i);

        }
    }
}
