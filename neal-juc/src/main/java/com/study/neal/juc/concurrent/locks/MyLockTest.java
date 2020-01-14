package com.study.neal.juc.concurrent.locks;

import com.study.neal.juc.concurrent.locks.subject001.spin.CasSpinLock;
import com.study.neal.juc.concurrent.locks.subject001.spin.ClhSpinLock;
import com.study.neal.juc.concurrent.locks.subject001.spin.McsSpinLock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
public class MyLockTest {

    static class Resource{
        int count = 0;

        public void increment() {
            count++;
        }

        public void print() {
            System.out.println(count);
        }

    }

    public static long concurrencyTest(final Lock lock, final int concurrency) {
        final Resource resource = new Resource();

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch stopLatch = new CountDownLatch(concurrency);

        for (int i = 0; i < concurrency; i++) {
            new Thread(() -> {
                    lock.lock();
                    try {
                        startLatch.await();

                        // do something
//                        System.out.printf("%s: count start\n", Thread.currentThread().getName());
                        for (int j = 0; j < 10000000; j++) resource.increment();
//                        System.out.printf("%s: count end\n", Thread.currentThread().getName());

                        stopLatch.countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                }
            ).start();
        }

        startLatch.countDown();
        long start = System.currentTimeMillis();

        try {
            stopLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        resource.print();
        final long used = end - start;
//        System.out.println(lock.getClass().getSimpleName() + " result: " + resource.count);
//        System.out.println("time used: " + used + "ms");
        return used;
    }

    public static void loopConcurrencyTest(final Lock lock, final int concurrency, int loop) {
        long sum = 0;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;

        for (int i = 0; i < loop; i++) {
            final long used = concurrencyTest(lock, concurrency);
            sum += used;
            min = Long.min(min, used);
            max = Long.max(max, used);
        }

        long avg = sum / loop;

        System.out.println(lock.getClass().getSimpleName() + " used time: ");
        System.out.println("\tmin: " + min);
        System.out.println("\tavg: " + avg);
        System.out.println("\tmax: " + max);
        System.out.println();
    }

    public static void main(String[] args) {

        Lock[] locks = {
                new CasSpinLock(),
                new ClhSpinLock(),
                new McsSpinLock(),
                new ReentrantLock()
        };

        for (final Lock lock : locks) {
            loopConcurrencyTest(lock, 20, 100);
        }
    }
}
