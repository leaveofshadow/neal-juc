package com.study.neal.juc.locks.subject002;


import com.study.neal.juc.locks.MyLock;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

/**
 * 可重入、互斥、悲观、非公平锁
 *
 * 依据synchronized 重量级锁的实现原理
 *
 * 抢到锁的当前线程 owner
 * 抢锁失败的阻塞队列 entrySet
 * owner 的重入次数 count
 *
 * 暂未实现
 * 等待队列 waitingSet
 *
 * 加锁实现：
 *      localCount = count.get()
 *      如果 localCount ==0 表示未加锁，线程使用cas(0, localCount++)抢锁,
 *           如果抢锁成功， 线程修改owner为当前线程，
 *           否则抢锁失败，进入阻塞队列，阻塞
 *      如果 localCount > 0
 *           如果owner是当前线程，表示重入 count++
 *           否则，抢锁失败进入阻塞队列，阻塞
 *
 * 释放锁实现
 *      如果owner 不是当前线程，表示没有加锁，抛出异常{@link IllegalMonitorStateException}
 *      否则
 *           count == 0  owner置为null
 *           count > 0 count--
 *
 * @author neal
 * @date 2019/11/23 14:51
 */
public class MyReentrantLock extends MyLock {

    /**
     * 抢到锁的当前线程
     */
    private Thread owner;

    /**
     * 锁的重入次数
     */
    private AtomicInteger count;

    /**
     * 阻塞队列 抢锁失败的线程阻塞队列
     */
    private Queue<Thread> blockedQueue = new LinkedBlockingQueue<>();

    /**
     * 等待队列 wait()方法可以释放锁（count为0，owner为null）,被唤醒时会重新抢锁
     *
     * 如果加锁的是当前线程，先加入等待队列，再释放锁（循环tryUnlock直到为true， 阻塞
     * 否则，抛出异常{@link IllegalMonitorStateException}
     *
     */
    private Queue<Thread> waitingQueue = new LinkedBlockingQueue<>();

    /**
     *
     * @return
     */

    @Override
    public boolean tryLock() {
        Thread currentThread = Thread.currentThread();
        int localCount = count.get();
        if (localCount == 0) {
            // 当前未加锁，使用cas抢锁
            if (count.compareAndSet(localCount, localCount + 1)) {
                // 抢锁成功，此时只有当前线程可以修改此值，仍要考虑可见性，所以使用volatile
                owner = currentThread;
                return true;
            }
        } else {
            // 判断owner是否是当前线程
            if (owner == currentThread) {
                // 表示重入，count直接+1，此时只有当前线程可以修改此值
                count.incrementAndGet();
                return true;
            }
        }
        return false;
    }

    @Override
    public void lock() {
        Thread currentThread = Thread.currentThread();
        // 避免伪唤醒使用循环包裹阻塞
        if (!tryLock()) {
            // 如果加锁失败，加入阻塞队列，并阻塞
            blockedQueue.add(currentThread);

            // 继续自旋抢锁
            for (;;) {
                /**
                 * 为了防止唤醒时的惊群效应，解锁时只唤醒队列头部等待的线程，
                 * 但并不是意味着是公平锁，第一次抢锁即未加入阻塞队列的线程仍然可以获得锁，破环公平性
                 *
                 * 所以只有被唤醒的头部线程可以去抢锁
                 */
                if (currentThread == blockedQueue.peek()) {
                    if (!tryLock()) {
                        LockSupport.park(currentThread);
                    } else {
                        // 如果唤醒后加锁成功，将线程从阻塞队列中移除
                        blockedQueue.remove();
                        return;
                    }
                } else {
                    LockSupport.park(currentThread);
                }
            }
        }
    }

    private boolean tryUnlock() {
        Thread currentThread = Thread.currentThread();
        if (owner != currentThread) {
            throw new IllegalMonitorStateException();
        } else {
            int localCount = count.get();
            int nextCount = localCount - 1;
            count.set(nextCount);
            if (nextCount == 0) {
                owner = null;   // 在此之后，新加入的线程可能会抢到锁
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void unlock() {
        if (tryUnlock()) {
            // 唤醒下一个线程
            Thread head = blockedQueue.peek();
            if (head != null) {
                LockSupport.unpark(head);
            }
        }
    }



    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }


}

