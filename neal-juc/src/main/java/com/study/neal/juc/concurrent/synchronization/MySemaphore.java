package com.study.neal.juc.concurrent.synchronization;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 信号量
 * 多用于限流 带个数限制的读锁
 *
 * 开始时有n个资源可用，每次acquire减少1个可用资源，当可用资源小于0时，加锁失败；release增加1个资源
 */
public class MySemaphore {

    private Sync sync;

    public MySemaphore(int permits) {
        this.sync = new Sync(permits);
    }

    public void acquire() {
        sync.acquireShared(1);
    }

    public void release() {
        sync.releaseShared(1);
    }

    private static class Sync extends AbstractQueuedSynchronizer {

        public Sync(int permits) {
            setState(permits);
        }

        @Override
        /**
         * cas+自旋
         */
        protected int tryAcquireShared(int acquires) {
            for (;;) {
                int available = getState();
                int remaining = available - acquires;
                if (remaining < 0 ||
                        compareAndSetState(available, remaining))
                    return remaining;
            }
        }

        @Override
        protected boolean tryReleaseShared(int releases) {
            for (;;) {
                int current = getState();
                int next = current + releases;
                if (next < current) // overflow
                    throw new Error("Maximum permit count exceeded");
                if (compareAndSetState(current, next))
                    return true;
            }
        }
    }

}
