package com.study.neal.juc.concurrent.synchronization;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 倒计数锁
 *
 * 默认n个线程持有共享锁，当全部释放锁之后，才能继续获得锁
 *
 * 相当于跑步比赛，跑道是资源，运动员是线程，倒计数跑完的运动员，当跑道上剩余的人到0时，才可以去颁奖
 *
 * 常见场景：启动同步器，让多个线程阻塞，直到释放之后，多个线程一起执行；停用同步器
 */
public class MyCountDownLatch {

    private Sync sync;

    public MyCountDownLatch(int count) {
        this.sync = new Sync(count);
    }

    /**
     * 释放锁
     */
    public void countDown() {
        sync.releaseShared(1);
    }

    /**
     * 加锁
     */
    public void await() {
        sync.acquireShared(1);
    }

    private static class Sync extends AbstractQueuedSynchronizer {

        public Sync(int count) {
            setState(count);
        }

        @Override
        protected int tryAcquireShared(int arg) {
            return getState() == 0 ? 1 : -1;
        }

        @Override
        protected boolean tryReleaseShared(int unused) {
            for (;;) {
                int c = getState();
                if (c == 0) {
                    return false; // 不需要唤醒其他线程
                }
                int nextc = c-1;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }
    }

}
