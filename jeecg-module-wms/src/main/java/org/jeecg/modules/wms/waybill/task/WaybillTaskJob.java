package org.jeecg.modules.wms.waybill.task;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrders;
import org.jeecg.modules.wms.outorder.service.IWmsOutOrdersService;
import org.jeecg.modules.wms.waybill.service.IWmsWaybillService;
import org.jeecg.modules.wms.waybill.task.handler.SimpleThreadCallable;
import org.jeecg.modules.wms.waybill.task.handler.WaybillThreadCallable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Mr.M
 * @version 1.0
 * @description 定时任务类
 * @date 2025/9/13 10:10
 */
@Component
@Slf4j
public class WaybillTaskJob {

    //注入线程池
    @Resource(name="waybillThreadPool")
    private ScheduledThreadPoolExecutor waybillThreadPool;

    @Autowired
    private IWmsOutOrdersService wmsOutOrdersService;

    @Autowired
    private IWmsWaybillService wmsWaybillService;

    // true表示正在执行，false表示执行完成
    // volatile 关键字确保在多线程环境下，isRunning 的值能够及时更新
    private volatile Boolean isRunning = false;

    private volatile List<Future<?>> pendingFutures = new ArrayList<>();


//    /**
//     * 定时获取面单
//     * 首先获取打包完成的波次，调用顺丰接口进行下单，获取面单 pdf并存储至minio
//     */
//    @Scheduled(cron = "0/20 * * * * ?")
//    public void createWaybills() {
//        log.info("开始执行定时任务，生成运单...");
//        //查询出库单状态为打包完成且未创建运单的出库单
//        WmsOutOrders wmsOutOrders = new WmsOutOrders();
//        wmsOutOrders.setStatus(WarehouseDictEnum.OUTBOUND_PACKED.getCode());//已打包
//        wmsOutOrders.setCreatedWaybill("0");// 未生成运单
//        //已打包未生成运单的出库单
//        IPage<WmsOutOrders> wmsOutOrdersIPage = wmsOutOrdersService.queryList(wmsOutOrders, 1, 100);
//
//        List<WmsOutOrders> records = wmsOutOrdersIPage.getRecords();
//        if (records.size()<=0){
//            return;
//        }
//        List<FutureTask<Object>> list = new ArrayList<>();
//        for (WmsOutOrders outOrder : records) {
//            //生成一个任务
//            WaybillThreadCallable waybillThreadCallable = new WaybillThreadCallable(wmsWaybillService,outOrder.getId());
//            //将任务封装成futureTask
//            FutureTask futureTask = new FutureTask(waybillThreadCallable);
//
//            //提交给线程池
//            waybillThreadPool.submit(futureTask);
//            list.add(futureTask);
//
//        }
//        //阻塞当前线程，等待所有任务执行完毕
////        FutureTaskUtils.block(list);
//
//    }
//    @Scheduled(cron = "0/2 * * * * ?")
    public void test2() {
        // 防止重入
        // 如果上一轮任务还没有完成，不再执行
        if (isRunning){
            log.info("上一轮任务还没有完成，不再执行");
            return;
        }
        log.info("开始执行定时任务，生成运单...");
        isRunning = true;
        // 定义一个list<future> 用于存储当前正在执行的任务
        List<Future<Boolean>> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            SimpleThreadCallable simpleThreadCallable = new SimpleThreadCallable();
            Future future = waybillThreadPool.submit(simpleThreadCallable);
            list.add(future);
        }

        // 判断这些任务是否完成
        for (Future<Boolean> future : list) {
            // 获取每个任务的执行结果
            try {
                // 阻塞方法
                Boolean result = future.get(30, TimeUnit.SECONDS);
                log.info("任务执行结果：{}", result);
            } catch (Exception e) {
                log.error("任务执行异常", e);
                // 取消任务
                future.cancel(true);
            }
        }
        // 执行到这里说明上一轮任务执行完了
        isRunning = false;
        log.info("上一轮任务执行完了");
    }

    /**
     * 定时生成运单
     */
    @Scheduled(cron = "0/20 * * * * ?")
    public void createWaybills() {
        // 防止重入
        // 如果上一轮任务还没有完成，不再执行
        if (isRunning){
            log.info("上一轮任务还没有完成，不再执行");
            return;
        }
        log.info("开始执行定时任务，生成运单...");
        isRunning = true;
        try {

            // 定义一个list<future> 用于存储当前正在执行的任务
            List<Future<Boolean>> list = new ArrayList<>();
            //查询出库单状态为打包完成且未创建运单的出库单
            WmsOutOrders wmsOutOrders = new WmsOutOrders();
            wmsOutOrders.setStatus(WarehouseDictEnum.OUTBOUND_PACKED.getCode());wmsOutOrders.setCreatedWaybill("0");
            //todo:出库单表添加status、createdWaybill联合索引
            IPage<WmsOutOrders> wmsOutOrdersIPage =
                    wmsOutOrdersService.queryList(wmsOutOrders, 1, 1000);
            List<WmsOutOrders> records = wmsOutOrdersIPage.getRecords();
            for (WmsOutOrders outOrder : records) {
                // 创建生成运单任务，订单id，包裹service
                WaybillThreadCallable waybillThreadCallable = new WaybillThreadCallable(wmsWaybillService, outOrder.getId());
                Future future = waybillThreadPool.submit(waybillThreadCallable);
                list.add(future);
            }

            // 判断这些任务是否完成
            for (Future<Boolean> future : list) {
                // 获取每个任务的执行结果
                try {
                    // 阻塞方法
                    Boolean result = future.get(30, TimeUnit.SECONDS);
                    log.info("任务执行结果：{}", result);
                } catch (Exception e) {
                    log.error("任务执行异常", e);
                    // 取消任务
                    future.cancel(true);
                }
            }
            // 执行到这里说明上一轮任务执行完了
            log.info("上一轮任务执行完了");
        } finally {
            isRunning = false;
        }

    }

}