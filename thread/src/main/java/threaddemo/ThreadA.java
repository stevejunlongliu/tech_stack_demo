package threaddemo;

public class ThreadA extends Thread {

    //构造函数指定线程名
    public ThreadA(String name) {
        super(name);
    }


    //最简单线程应用
    public static void threadTest() {
        ThreadA A = new ThreadA("A线程");
        ThreadA B = new ThreadA("B线程");
        A.start();
        B.start();
    }




    @Override
    public void run() {

        for (int i = 0; i < 10; i++) {
            System.out.println(getName() + "：正在执行！" + i);

        }
    }
}

