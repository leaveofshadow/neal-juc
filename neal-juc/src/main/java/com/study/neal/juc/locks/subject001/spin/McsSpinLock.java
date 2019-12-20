package com.study.neal.juc.locks.subject001.spin;

import com.study.neal.juc.locks.MyLock;
import com.study.neal.juc.locks.MyLockTest;

import java.util.concurrent.atomic.AtomicReference;

/**
 * MSC Spin Lock
 *
 * 同CLH，
 *
 * @author neal
 * @date 2018/10/29 0:29
 */
public class McsSpinLock extends MyLock {

    private static class McsNode {
        private volatile boolean isLocked = true;
        private McsNode next;
    }

    private final ThreadLocal<McsNode> local = ThreadLocal.withInitial(McsNode::new);
    private final AtomicReference<McsNode> tail = new AtomicReference<>();

    @Override
    public void lock() {
        final McsNode current = local.get();
        McsNode pre = tail.getAndSet(current);  // 加入队列
        if (pre != null) {                      // 初始时tail为null，使得第一个加入队列的直接获得锁
            pre.next = current;
            while (current.isLocked) {          // 如果当前值是锁着的则自旋，等待上一个线程释放当前的锁

            }
        }

    }

    /**
     * 需要释放下一个线程的锁
     * 如果不存在下一个节点的话，需要进行判断是否有新的节点加入，并自旋等待加入操作完成
     */
    @Override
    public void unlock() {
        final McsNode current = local.get();
        if (current.next == null) {
            if (tail.compareAndSet(current, null)) {    // 如果当前线程就在队尾则直接结束
                return;
            } else {
                while (current.next == null) {                  // 双重检验，此时有新加入的线程节点，则需等到执行完 pre.next = current;

                }
            }
        }
        current.next.isLocked = false;                          // 释放下一个线程的锁
        current.next = null;                                    // 释放连接
    }

    public static void main(String[] args) {
        final MyLock lock = new McsSpinLock();

        final int concurrency = 10;

        // best:    55ms
        // avg:     76ms
        // worst:   116ms
        MyLockTest.concurrencyTest(lock, concurrency);
    }

}
