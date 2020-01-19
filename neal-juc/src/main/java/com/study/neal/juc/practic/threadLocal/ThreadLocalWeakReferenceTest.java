package com.study.neal.juc.practic.threadLocal;

import java.lang.ref.WeakReference;

/**
 * ThreadLocal实现原理是，每个线程都有一个基于开放地址法实现的hash map用于存储线程隔离的变量，
 * 其中map是个Entry对象，该Entry的key是ThreadLocal对象，Entry继承自WeakReference
 */
public class ThreadLocalWeakReferenceTest {

    static void triggerGc() {
        for (int i = 0; i < 2; i++) {
            byte[] tmp = new byte[1024 * 1024 * 256]; // 256兆
            System.gc(); // 8G堆 128兆。full GC
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

  static class Task implements Runnable {
      public void run() {
          Thread currentThread = Thread.currentThread();
          ThreadLocal<Integer> integerThreadLocal = ThreadLocal.withInitial(() -> 10);
          System.out.println("before gc: " + integerThreadLocal.get());     // 调用get时才创建map，设置初始值

          WeakReference<ThreadLocal> localWeakReference = new WeakReference<>(integerThreadLocal);
          integerThreadLocal = null;    // 释放强引用
          triggerGc();
          // gc触发后ThreadLocal会被回收，报NullPointerException

          System.out.println("after gc weak local: " + localWeakReference.get().get());
      }
  }

    public static void main(String[] args) throws Exception {

        Thread t = new Thread(new Task());
        t.start();
        t.join();
    }

}
