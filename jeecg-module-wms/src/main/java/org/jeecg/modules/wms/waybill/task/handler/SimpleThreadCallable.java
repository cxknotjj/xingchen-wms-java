package org.jeecg.modules.wms.waybill.task.handler;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

/**
 * @author Mr.M
 * @version 1.0
 * @description 任务执行体
 * @date 2025/9/15 8:57
 */
@Slf4j
public class SimpleThreadCallable implements Callable {
    @Override
    public Object call() throws Exception {

        try {
            //当前线程名
            String name = Thread.currentThread().getName();
            log.info(name+"执行定时任务...");
            //模拟执行时间
            Thread.sleep(10000);
            return true;
        } catch (InterruptedException e) {
            return false;
        }

    }
}
