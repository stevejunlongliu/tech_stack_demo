package threaddemo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolDemo {

    public static void test() {
        ExecutorService service = Executors.newFixedThreadPool(3);//包含2个线程对象
        RunableDemo r = new RunableDemo();
        service.submit(r);
        service.submit(r);
        service.submit(r);
        service.submit(r);
        service.shutdown();
    }

}
