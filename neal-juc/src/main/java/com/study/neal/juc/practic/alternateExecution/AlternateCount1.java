package com.study.neal.juc.practic.alternateExecution;

import java.util.concurrent.CountDownLatch;

/**
 * ����ִ��
 */
public class AlternateCount1 {

//    private static final int MAX_VALUE = 100;
    private static final int MAX_VALUE = 10000000;

    // �������
    public static int count = 0;

    public static volatile boolean writeFinished = false;   // ����happen-before��ԭ��

    public static void main(String[] args) throws Exception {

        CountDownLatch stopLatch = new CountDownLatch(2);

        // �߳�1 - д������
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

        // �߳�2 - ��ȡ����
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
