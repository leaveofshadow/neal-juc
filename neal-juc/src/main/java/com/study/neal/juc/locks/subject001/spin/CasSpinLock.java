package com.study.neal.juc.locks.subject001.spin;

import com.study.neal.juc.locks.MyLock;
import com.study.neal.juc.locks.MyLockTest;

import java.util.concurrent.atomic.AtomicReference;

/**
 * cas 自旋的非公平锁
 *
 * 非公平锁与公平锁的区别在于 新晋需要获取锁的进程/线程会有多次机会去抢占锁（非公平的）
 * 公平锁是FIFO的，更耗费性能
 *
 * 由于自旋锁只是将当前线程不停地执行循环体，不进行线程状态的改变，所以响应速度更快。
 * 但当线程数不停增加时，性能下降明显，因为每个线程都需要执行，占用CPU时间。
 * 如果线程竞争不激烈，并且保持锁的时间段。适合使用自旋锁。
 *
 * @author neal
 * @since  2018/10/26 14:28
 */
public class CasSpinLock extends MyLock {

    private final AtomicReference<Thread> shared;

    public CasSpinLock() {
        shared = new AtomicReference<>(null);
    }

    @Override
    public void lock() {
        while (! shared.compareAndSet(null, Thread.currentThread()));
    }

    @Override
    public void unlock() {
//        shared.compareAndSet(Thread.currentThread(), null);
        shared.set(null);
    }

    public static void main(String[] args) {
        final MyLock lock = new CasSpinLock();

        final int concurrency = 10;

        // best:    41ms
        // avg:     64ms
        // worst:   108ms
        MyLockTest.concurrencyTest(lock, concurrency);
    }

}
