package com.study.neal.juc.practic.alternateExecution;

/**
 * ����ִ��
 */
public class AlternateExecution2 {
    // �������
    public static String content = "��";

    public static volatile boolean writeFinished = false;   // ����happen-before��ԭ��

    public static void main(String[] args) {
        // �߳�1 - д������
        new Thread(() -> {
            while (true) {
                if (!writeFinished) {
                    content = "��ǰʱ��" + String.valueOf(System.currentTimeMillis() / 1000);
                    try {
                        Thread.sleep(1000L);
                    } catch (Exception e) {

                    }
                    writeFinished = true;
                }
            }
        }).start();

        // �߳�2 - ��ȡ����
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
