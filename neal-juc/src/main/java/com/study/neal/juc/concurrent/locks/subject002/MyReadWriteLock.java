package com.study.neal.juc.concurrent.locks.subject002;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * 当前读线程数量 readCount
 * 当前写线程数量 writeCount
 * 当前写占用线程 owner 实现写锁的排他性
 * 等待队列  blockedQueue
 */
public class MyReadWriteLock {

    public static class WaitNode {
        static final boolean SHARED = true;
        static final boolean EXCLUSIVE = true;

        Thread currentThread;

        /**
         * 独占，写锁 false
         * 共享，读锁 true
         */
        boolean state;

        // 加锁、释放锁的数量
        int count;

        public WaitNode(Thread currentThread, boolean state, int count) {
            this.currentThread = currentThread;
            this.state = state;
            this.count = count;
        }
    }

    /**
     * 此处readCount 和 writeCount使用两个原子类实现，但是并不能保证加锁的原子性
     * 后续可以参考 {@link java.util.concurrent.locks.ReadWriteLock} 共用一个int型count的实现
     */
    private AtomicInteger readCount = new AtomicInteger(0);

    private AtomicInteger writeCount = new AtomicInteger(0);

    private Thread owner;

    private Queue<WaitNode> blockedQueue = new LinkedBlockingQueue<>();

    public boolean tryLock(int acquires) {
        int currentReadCount = readCount.get();
        if (currentReadCount > 0) {
            // 存在读锁，不能获取写锁
            return false;
        }
        final Thread currentThread = Thread.currentThread();
        int currentWriteCount = writeCount.get();
        if (currentWriteCount == 0) {
            int nextWriteCount = currentWriteCount + acquires;
            if (writeCount.compareAndSet(currentWriteCount, nextWriteCount)) {
                // 抢到写锁
                owner = currentThread;
                return true;
            }
        } else {
            if (owner == currentThread) {
                // 重入
                int nextWriteCount = currentWriteCount + acquires;
                writeCount.set(nextWriteCount);
                return true;
            }
        }
        // 抢写锁失败
        return false;
    }

    public boolean tryUnlock(int releases) {
        final Thread currentThread = Thread.currentThread();
        if (owner != currentThread) {
            throw new IllegalMonitorStateException();
        } else {
            int currentWriteCount = writeCount.get();
            int nextCount = currentWriteCount - releases;
            writeCount.set(nextCount);
            if (nextCount == 0) {
                owner = null;
                // 在此之后，新加入的线程可能会抢到锁
                return true;
            } else {
                return false;
            }
        }
    }

    public int tryLockShared(int acquires) {
        Thread currentThread = Thread.currentThread();
        // 多个线程同时抢读锁，只有修改成功才算加锁成功，所以此处使用cas + 自旋
        for (; ; ) {
            if (writeCount.get() > 0 && currentThread != owner) {
                return -1;
            }
            int currentReadCount = readCount.get();
            if (readCount.compareAndSet(currentReadCount, currentReadCount + acquires)) {
                return 1;
            }
        }
    }

    public boolean tryUnLockShared(int releases) {
        // cas + 自旋释放锁
        for (; ; ) {
            int rc = readCount.get();
            int nextc = rc - releases;
            if (readCount.compareAndSet(rc, nextc)) {
                return nextc == 0;
            }
        }
    }

    public void lock() {
        int acquires = 1;
        //尝试获取独占锁，若成功，退出方法，    若失败...
        if (!tryLock(acquires)) {
            //标记为独占锁
            WaitNode waitNode = new WaitNode(Thread.currentThread(), WaitNode.EXCLUSIVE, acquires);
            blockedQueue.offer(waitNode);    //进入等待队列

            //循环尝试拿锁
            for (; ; ) {
                //若队列头部是当前线程
                WaitNode head = blockedQueue.peek();
                if (head != null && head.currentThread == Thread.currentThread()) {
                    if (!tryLock(acquires)) {      //再次尝试获取 独占锁
                        LockSupport.park();     //若失败，挂起线程
                    } else {     //若成功获取
                        blockedQueue.poll();     //  将当前线程从队列头部移除
                        return;         //并退出方法
                    }
                } else {  //若不是队列头部元素
                    LockSupport.park();     //将当前线程挂起
                }
            }
        }
    }

    public boolean unLock() {
        int releases = 1;
        if (tryUnlock(releases)) {
            // 唤醒下一个线程
            WaitNode head = blockedQueue.peek();
            if (head != null) {
                LockSupport.unpark(head.currentThread);
            }
            return true;
        }
        return false;
    }

    public void lockShared() {
        int acquires = 1;
        Thread currentThread = Thread.currentThread();

        if (tryLockShared(acquires) < 0) {
            // 加入阻塞队列
            WaitNode waitNode = new WaitNode(currentThread, WaitNode.SHARED, acquires);
            blockedQueue.offer(waitNode);

            for (; ; ) {
                WaitNode head = blockedQueue.peek();
                if (head != null && head.currentThread == currentThread) {
                    if (tryLockShared(acquires) >= 0) {
                        blockedQueue.poll();
                        // 唤醒其它读线程
                        WaitNode next = blockedQueue.peek();
                        if (next != null && next.state == WaitNode.SHARED) {
                            LockSupport.unpark(next.currentThread);
                        }
                        return;
                    } else {
                        LockSupport.park(currentThread);
                    }
                } else {
                    LockSupport.park(currentThread);
                }

            }
        }
    }

    public boolean unLockShared() {
        int releases = 1;
        if (tryUnLockShared(releases)) {
            // TODO: 唤醒其他线程
            WaitNode head = blockedQueue.peek();
            if (head != null) {
                LockSupport.unpark(head.currentThread);
            }
            return true;
        }
        return false;
    }

}
