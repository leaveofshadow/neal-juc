package com.study.neal.juc.practic.sync;

import java.util.concurrent.TimeUnit;

/**
 * 父
 */
public class ReetrantParent {

    static class Widgest {

        public synchronized void doSomething() {
            System.out.println(Thread.currentThread() + " Widgest doSomething: " + this);
            System.out.println(Thread.currentThread() + " Widgest doSomething runtime class: " + this.getClass().getSimpleName());
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class LoggingWidgest extends Widgest {
        public synchronized void doSomething() {
            System.out.println(Thread.currentThread() + " LoggingWidgest doSomething: " + this);
            System.out.println(Thread.currentThread() + " LoggingWidgest doSomething super: " + super.toString());
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            super.doSomething();
        }
    }

    public static void main(String[] args) {
        Widgest widgest = new Widgest();
        LoggingWidgest loggingWidgest = new LoggingWidgest();

        new Thread(() -> {
            widgest.doSomething();
        }).start();

        new Thread(() -> {
            loggingWidgest.doSomething();
        }).start();
    }

}
