package com.study.neal.juc.practic.alternateExecution;

/**
 * 交替执行
 */
public class AlternateExecution1 {
    // 共享变量
    public static String content = "空";

    public static boolean writeFinished = false;

    public static final Object lock = new Object();


    public static void main(String[] args) {
        // 线程1 - 写入数据
        new Thread(() -> {
            try {
                while (true) {
                    synchronized (lock) {
                        while (writeFinished) {
                            lock.wait();
                        }
                        content = "当前时间" + String.valueOf(System.currentTimeMillis() / 1000);
                        Thread.sleep(1000L);
                        writeFinished = true;
                        lock.notifyAll();
                    }


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // 线程2 - 读取数据
        new Thread(() -> {
            try {
                while (true) {
                    synchronized (lock) {
                        while (!writeFinished) {
                            lock.wait();
                        }
                        System.out.println(content);
                        writeFinished = false;
                        lock.notifyAll();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }
}
