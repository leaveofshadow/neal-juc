package com.study.neal.juc.locks.subject003;

import com.study.neal.juc.locks.MyLock;
import com.study.neal.juc.locks.subject003.source.AbstractQueuedSynchronizer;


/**
 * @author yedunyao
 * @since 2019/12/20 16:14
 */
public class MyReentranLockUseAqs extends MyLock {

    private Sync sync;

    public MyReentranLockUseAqs() {
        sync = new NonfairSync();
    }

    public MyReentranLockUseAqs(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }

    abstract static class Sync extends AbstractQueuedSynchronizer {

        public abstract void lock();

        @Override
        protected boolean tryRelease(int arg) {
            if (!isHeldExclusively()) {
                throw new IllegalMonitorStateException();
            }
            int currentState = getState();
            int nextState = currentState - arg;
            boolean free = false;
            if (nextState == 0) {
                free = true;      // 可以唤醒下一个线程
                setExclusiveOwnerThread(null);
            }
            // 先修改 owner，否则的话可能会修改后续抢到锁的owner置为null  修改完state 才真正算释放锁
            setState(nextState);
            return free;
        }

        @Override
        protected final boolean isHeldExclusively() {
            return getExclusiveOwnerThread() == Thread.currentThread();
        }
    }

    /**
     * 非公平锁
     */
    static class NonfairSync extends Sync {

        public void lock() {
            // 尝试快速抢锁
            if (compareAndSetState(0, 1))
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }

        /**
         * 直接抢锁，cas 抢占 {@link AbstractQueuedSynchronizer#state}
         *
         * @param arg the acquire argument. This value is always the one
         *        passed to an acquire method, or is the value saved on entry
         *        to a condition wait.  The value is otherwise uninterpreted
         *        and can represent anything you like.
         * @return
         */
        @Override
        protected boolean tryAcquire(int arg) {
            Thread currentThread = Thread.currentThread();
            int currentState = getState();
            if (currentState == 0) {
                // 抢锁
                if (compareAndSetState(0, arg)) {
                    setExclusiveOwnerThread(currentThread);
                    return true;
                }

            } else {
                // 判断是否是当前线程占用的锁
                if (isHeldExclusively()) {
                    // 重入，状态+1
                    setState(currentState + arg);
                    return true;
                }
            }
            return false;
        }


    }

    static final class FairSync extends Sync {
        private static final long serialVersionUID = -3000897897090466540L;

        public void lock() {
            acquire(1);
        }

        /**
         * 如果队列为空或者是队头可以抢锁
         */
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                // hasQueuedPredecessors查看是否有排队时间更长的队列
                if (!hasQueuedPredecessors() &&
                        compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                setState(nextc);
                return true;
            }
            return false;
        }
    }


    @Override
    public void lock() {
        sync.lock();
    }

    @Override
    public void unlock() {
        sync.release(1);
    }

}
