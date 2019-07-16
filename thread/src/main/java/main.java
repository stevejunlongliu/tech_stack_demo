import lock.*;
import threaddemo.*;
import threadpool.ThreadPoolExecutorDemo;

import java.io.IOException;

public class main {


    public static void main(String[] args) throws InterruptedException, IOException {
        //RunableDemo.test();
        // ThreadA.threadTest();
        // ThreadPoolDemo.test();
        //LockRunnableDemo.test();
        // ThreadPoolExecutorDemo.test();

        //  ThreadTooManyDemo.test();
        //JoinDemo.test();

        // ConcurrentLockDemo.test();
        // AtomicIntegerDemo.test();
        // CountDownLatchDemo.test();
        //CyclicBarrierDemo.test();
        SemaphoreDemo.test();
        System.out.println("hello world!");

    }


}
