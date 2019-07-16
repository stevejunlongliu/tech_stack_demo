package lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentLockDemo {

    public static void test() {
        final Lock lock = new ReentrantLock();

        Thread a = new Thread(new Runnable() {
            public void run() {
                if (lock.tryLock()) {
                    System.out.println(Thread.currentThread().getName() + " get lock");

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    lock.unlock();
                }
            }
        });

        //尝试锁失败
        Thread b = new Thread(new Runnable() {
            public void run() {
                if (lock.tryLock()) {
                    System.out.println(Thread.currentThread().getName() + " get lock");
                    try {
                        Thread.sleep(1010);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    lock.unlock();
                } else {
                    System.out.println(Thread.currentThread().getName() + " lock fail");
                }
            }
        });

        //自旋锁
        Thread c = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (lock.tryLock()) {
                        System.out.println(Thread.currentThread().getName() + " get lock");
                        try {
                            Thread.sleep(1020);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        lock.unlock();
                        break;
                    } else {
                        System.out.println(Thread.currentThread().getName() + " lock fail");
                    }
                }
            }
        });

        a.start();
        b.start();
        c.start();

    }
}
