package lock;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIntegerDemo {
    public static void test() {
//安全？相对于i++来说是的
        final AtomicInteger count = new AtomicInteger(0);
        Runnable a = new Runnable() {
            public void run() {
                System.out.println(Thread.currentThread().getName() + ":" + count.getAndIncrement());
                ;
            }
        };

        //并不是按顺序输出的，因为线程竞争结果不一样，但能保证是累加30次的值
        //如果是++的操作，很有可能会导致数据乱掉，因为++操作是不加锁的i=i+1
        for (int i = 0; i < 30.; i++) {
            Thread thread = new Thread(a);
            thread.start();
        }
    }
}
