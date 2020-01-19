package com.study.neal.juc.practic.alternateExecution;

/**
 * 交替执行
 */
public class AlternateExecution2 {
    // 共享变量
    public static String content = "空";

    public static volatile boolean writeFinished = false;   // 利用happen-before的原则

    public static void main(String[] args) {
        // 线程1 - 写入数据
        new Thread(() -> {
            while (true) {
                if (!writeFinished) {
                    content = "当前时间" + String.valueOf(System.currentTimeMillis() / 1000);
                    try {
                        Thread.sleep(1000L);
                    } catch (Exception e) {

                    }
                    writeFinished = true;
                }
            }
        }).start();

        // 线程2 - 读取数据
        new Thread(() -> {
            while (true) {
                if (writeFinished) {
                    System.out.println(content);
                    writeFinished = false;
                }
            }
        }).start();

    }
}
