package com.study.neal.juc.practic.alternateExecution;

/**
 * ����ִ��
 */
public class AlternateExecution1 {
    // �������
    public static String content = "��";

    public static boolean writeFinished = false;

    public static final Object lock = new Object();


    public static void main(String[] args) {
        // �߳�1 - д������
        new Thread(() -> {
            try {
                while (true) {
                    synchronized (lock) {
                        while (writeFinished) {
                            lock.wait();
                        }
                        content = "��ǰʱ��" + String.valueOf(System.currentTimeMillis() / 1000);
                        Thread.sleep(1000L);
                        writeFinished = true;
                        lock.notifyAll();
                    }


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // �߳�2 - ��ȡ����
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
