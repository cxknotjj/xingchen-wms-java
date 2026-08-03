package org.jeecg.modules.wms.lock;

public class TicketSeller implements Runnable {
    private int tickets = 10; // 总票数

    @Override
    public void run() {
        while (tickets > 0) {
            sellTicket(); // 调用加锁的卖票方法
        }
    }

    // 核心：使用 synchronized 修饰方法
    // 这里的锁对象默认是 this (即当前的 TicketSeller 实例)
    private synchronized void sellTicket() {
        if (tickets > 0) {
            // 模拟卖票耗时
            try {
                Thread.sleep(50); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " 卖出了第 " + tickets + " 张票");
            tickets--;
        }
    }

    public static void main(String[] args) {
        // 注意：必须传入同一个 TicketSeller 对象，锁才会生效！
        TicketSeller seller = new TicketSeller(); 

        Thread t1 = new Thread(seller, "窗口A");
        Thread t2 = new Thread(seller, "窗口B");
        Thread t3 = new Thread(seller, "窗口C");

        t1.start();
        t2.start();
        t3.start();
    }
}