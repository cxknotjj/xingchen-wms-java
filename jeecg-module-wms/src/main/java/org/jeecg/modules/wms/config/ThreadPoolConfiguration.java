package org.jeecg.modules.wms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 波次处理任务线程池
 *
 */
@Configuration
public class ThreadPoolConfiguration {

    @Bean("waybillThreadPool")
    public ScheduledThreadPoolExecutor waveThreadPool() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);
        //配置最大线程数
        executor.setMaximumPoolSize(10);

        //配置线程池中的线程的名称前缀
//        executor.set("counter-total-synchronize-");
        // 设置拒绝策略：放弃任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());

        return executor;
    }
}
