package com.study.neal.juc.practic.alternateExecution;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * 交替执行
 * <p>
 * 使用Semaphore
 */
public class AlternateCount2 {

    private static final int MAX_VALUE = 100;

    // 共享变量
    public static int count = 0;

    public static void main(String[] args) throws Exception {

        CountDownLatch stopLatch = new CountDownLatch(2);

        Semaphore start1 = new Semaphore(1);
        Semaphore start2 = new Semaphore(0);

        // 线程1 - 写入数据
        new Thread(() -> {
            while (true) {
                try {
                    start1.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (count >= MAX_VALUE) {
                    start2.release();
                    break;
                }
                count++;
                System.out.println("thread-1: " + count);
                start2.release();
            }
            stopLatch.countDown();
        }).start();

        // 线程2 - 读取数据
        new Thread(() -> {
            while (true) {
                try {
                    start2.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (count >= MAX_VALUE) {
                    start1.release();
                    break;
                }
                count++;
                System.out.println("thread-2: " + count);
                start1.release();
            }
            stopLatch.countDown();
        }).start();

        stopLatch.await();
        System.out.println(count);

    }
}
