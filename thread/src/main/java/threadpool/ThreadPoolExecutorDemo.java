package threadpool;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolExecutorDemo {

    //单一线程复用
    public static void cacheThreadPoolTest() {

        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    System.out.println(Thread.currentThread().getName() + ":running");
                }
            });
        }

        cachedThreadPool.shutdown();

    }

    //固定线程数线程池
    public static void newFixedThreadPoolTest() {
        ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 10; i++) {
            newFixedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName() + ":running");
                }
            });
        }

        newFixedThreadPool.shutdown();
    }


    //按计划执行的线程池
    public static void newScheduledThreadPoolTest() {

        ScheduledExecutorService x = Executors.newScheduledThreadPool(3);

        //延迟1秒后每3秒执行一次
        for (int i = 0; i < 10; i++) {
            x.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    System.out.println(Thread.currentThread().getName() + ":running ");
                }
            }, 1, 3, TimeUnit.SECONDS);
        }

        // x.shutdown();
    }

    static class tmpRunnable implements Runnable {

        public void run() {
            System.out.println(System.currentTimeMillis() + Thread.currentThread().getName() + ":is running");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //缓冲队列线程池
    //    几个核心参数：
//        corePoolSize：核心线程数（不是初始线程数,也是根据需要一个个线程启动起来的）
//        maximumPoolSize ：线程池中允许的最大线程数，当阻塞队列满了后，会新启动线程直到最大线程数，之后会对请求进行拒绝
//        workQueue :阻塞队列，用来存放无法及时处理请求，如设为无界，可能会有内存溢出的风险

    public static void threadPoolExecutorTest() throws IOException {
        BlockingQueue<Runnable> bq = new ArrayBlockingQueue<Runnable>(6);


        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 4, 50, TimeUnit.MILLISECONDS, bq);

        tmpRunnable tmpRunnable = new tmpRunnable();

        for (int i = 0; i < 12; i++) {
            tpe.execute(tmpRunnable);
        }
        //System.in.read(); //阻塞主线程
        tpe.shutdown();
    }

    static class NameThreadFactory implements ThreadFactory {
        private final AtomicInteger mThreadNum = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "mythread" + mThreadNum.incrementAndGet());
            System.out.println(t.getName() + "has been created");
            return t;
        }
    }


    //拒绝策略
    static class MyIngorePoly implements RejectedExecutionHandler {

        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            doLog(r, executor);
        }

        private void doLog(Runnable r, ThreadPoolExecutor e) {
            // 可做日志记录等
            System.err.println(r.toString() + " rejected");
//          System.out.println("completedTaskCount: " + e.getCompletedTaskCount());
        }
    }

    //缓冲队列线程池--超出拒绝
    public static void threadPoolExecutorTestReject() {
        BlockingQueue<Runnable> bq = new ArrayBlockingQueue<Runnable>(10);

        ThreadPoolExecutor tpe = new ThreadPoolExecutor(2, 4, 100, TimeUnit.SECONDS, bq, new NameThreadFactory(), new MyIngorePoly());


        for (int i = 0; i < 10; i++) {
            tmpRunnable tmpRunnable = new tmpRunnable();
            tpe.execute(tmpRunnable);
        }

        tpe.shutdown();
    }

    public static void test() throws IOException {
        //cacheThreadPoolTest();
        //newFixedThreadPoolTest();
        //newScheduledThreadPoolTest
        threadPoolExecutorTest();
        //threadPoolExecutorTestReject();
    }
}

