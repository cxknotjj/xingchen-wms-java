package org.jeecg.modules.wms.wmstask.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.inorder.entity.WmsStockInOrders;
import org.jeecg.modules.wms.inorder.service.IWmsStockInOrderItemsService;
import org.jeecg.modules.wms.inorder.service.IWmsStockInOrdersService;
import org.jeecg.modules.wms.wmstask.entity.WmsTasks;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.wms.wmstask.entity.WmsTasksRecords;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description: 任务表
 * @Author: jeecg-boot
 * @Date: 2026-07-24
 * @Version: V1.0
 */
public interface IWmsTasksService extends IService<WmsTasks> {


    /**
     * 创建收货任务
     *
     * @param orderId
     * @param operator
     */
    public void createReceiveTask(String orderId, String operator);

    /**
     * 查询待办理任务列表
     *
     * @param wmsTasks
     * @return
     */
    public IPage<WmsTasks> list(WmsTasks wmsTasks, Integer pageNo, Integer pageSize);

    /**
     * 收货方法
     */
    public void receive(WmsTasksRecords wmsTasksRecords);
    /**
     * 执行任务
     */
    public WmsTasks execute(WmsTasksRecords wmsTasksRecords);

    /**
     * 上架方法
     * @param wmsTasksRecords
     */
    void shelf(WmsTasksRecords wmsTasksRecords);
}
