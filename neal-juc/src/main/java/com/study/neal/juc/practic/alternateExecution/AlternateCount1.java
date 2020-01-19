package com.study.neal.juc.practic.alternateExecution;

import java.util.concurrent.CountDownLatch;

/**
 * 交替执行
 */
public class AlternateCount1 {

//    private static final int MAX_VALUE = 100;
    private static final int MAX_VALUE = 10000000;

    // 共享变量
    public static int count = 0;

    public static volatile boolean writeFinished = false;   // 利用happen-before的原则

    public static void main(String[] args) throws Exception {

        CountDownLatch stopLatch = new CountDownLatch(2);

        // 线程1 - 写入数据
        new Thread(() -> {
            while (true) {
                if (!writeFinished) {
                    if (count >= MAX_VALUE) {
                        writeFinished = true;
                        break;
                    }
                    count++;
//                    System.out.println("thread-1: " + count);
                    writeFinished = true;
                }
            }
            stopLatch.countDown();
        }).start();

        // 线程2 - 读取数据
        new Thread(() -> {
            while (true) {
                if (writeFinished) {
                    if (count >= MAX_VALUE) {
                        writeFinished = false;
                        break;
                    }
                    count++;
//                    System.out.println("thread-2: " + count);
                    writeFinished = false;
                }
            }
            stopLatch.countDown();
        }).start();

        stopLatch.await();
        System.out.println(count);

    }
}
