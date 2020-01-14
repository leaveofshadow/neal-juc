package com.study.neal.juc.concurrent.locks.subject001.spin;

import com.study.neal.juc.concurrent.locks.MyLock;
import com.study.neal.juc.concurrent.locks.MyLockTest;

import java.util.concurrent.atomic.AtomicReference;

/**
 * CLH Spin Lock
 *
 * 公平自旋锁，利用FIFO队列的方式，保证线程的顺序执行，先加入队列的先执行。
 * 队列中的每个node代表线程。
 *
 * {@code lock}
 * 每当有线程需要获取锁则进入队列，即加入队尾,利用CAS操作保证原子性。
 * 所以尾指针是共享的，指向代表最后一个加入的线程（node）。
 * 而各个线程利用ThreadLocal方法，保存自己的node。
 * 通过判断上一个线程是否释放锁，决定是自旋等待还是获取锁后的继续执行。
 *
 */
public class ClhSpinLock extends MyLock {
//    private final ThreadLocal<Node> prev;
    private final ThreadLocal<Node> node;
    // 默认存在一个未加锁的node在队尾，省去判断队列是否为空
    private final AtomicReference<Node> tail = new AtomicReference<Node>(new Node(Thread.currentThread().getName()));

    public ClhSpinLock() {
        this.node = new ThreadLocal<Node>() {
            protected Node initialValue() {
                System.out.println(Thread.currentThread().getName() + " entry queue");
                return new Node(Thread.currentThread().getName());
            }
        };

//        this.prev = test ThreadLocal<Node>() {
//            protected Node initialValue() {
//                return null;
//            }
//        };
    }

    /**
     * 1.初始状态 tail指向一个node(head)节点
     * +------+
     * | head | <---- tail
     * +------+
     *
     * 2.lock-thread加入等待队列: tail指向新的Node，同时Prev指向tail之前指向的节点
     * +----------+
     * | Thread-A |
     * | := Node  | <---- tail
     * | := Prev  | -----> +------+
     * +----------+        | head |
     *                     +------+
     *
     *             +----------+            +----------+
     *             | Thread-B |            | Thread-A |
     * tail ---->  | := Node  |     -->    | := Node  |
     *             | := Prev  | ----|      | := Prev  | ----->  +------+
     *             +----------+            +----------+         | head |
     *                                                          +------+
     * 3.寻找当前node的prev-node然后开始自旋
     *
     */
    public void lock() {
        final Node node = this.node.get();
        node.locked = true;
//        System.out.println(node);
        Node pred = this.tail.getAndSet(node);
//        this.prev.set(pred);
        // 自旋
        while (pred.locked);
    }

    public void unlock() {
        Node node = this.node.get();
        node.locked = false;

        // 释放连接
        node = null;
        this.node.set(node);
//        this.prev.set(null);
//        System.out.println(Thread.currentThread().getName() + " leave queue");
//        this.node.set(this.prev.get());
    }

    private static class Node {
        private volatile boolean locked;
        private final String name;

        public Node(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "locked=" + locked +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    public static void main(String[] args) {
        final MyLock lock = new ClhSpinLock();

        final int concurrency = 10;

        // best:    52ms
        // avg:     82ms
        // worst:   124ms
        MyLockTest.concurrencyTest(lock, concurrency);
    }

}
