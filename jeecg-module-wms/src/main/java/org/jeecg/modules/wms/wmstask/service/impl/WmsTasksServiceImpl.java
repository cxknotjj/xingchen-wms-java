package org.jeecg.modules.wms.wmstask.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import dm.jdbc.util.StringUtil;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.inorder.entity.WmsStockInOrderItems;
import org.jeecg.modules.wms.inorder.entity.WmsStockInOrders;
import org.jeecg.modules.wms.inorder.service.IWmsStockInOrderItemsService;
import org.jeecg.modules.wms.inorder.service.IWmsStockInOrdersService;
import org.jeecg.modules.wms.inventory.entity.WmsInventoryTrans;
import org.jeecg.modules.wms.inventory.service.IWmsInventoryTransService;
import org.jeecg.modules.wms.inventory.service.impl.WmsInventoryTransByReceiving;
import org.jeecg.modules.wms.inventory.vo.WmsInventoryTransParam;
import org.jeecg.modules.wms.warehouse.entity.WmsStorageLocations;
import org.jeecg.modules.wms.warehouse.service.IWmsStorageLocationsService;
import org.jeecg.modules.wms.wmstask.entity.WmsTasks;
import org.jeecg.modules.wms.wmstask.entity.WmsTasksRecords;
import org.jeecg.modules.wms.wmstask.mapper.WmsTasksMapper;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksRecordsService;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description: 任务表
 * @Author: jeecg-boot
 * @Date: 2026-07-24
 * @Version: V1.0
 */
@Service
public class WmsTasksServiceImpl extends ServiceImpl<WmsTasksMapper, WmsTasks> implements IWmsTasksService {
    @Autowired
    private IWmsStockInOrdersService wmsStockInOrdersService;

    @Autowired
    private IWmsStockInOrderItemsService wmsStockInOrderItemsService;

    @Autowired
    private IWmsStorageLocationsService wmsStorageLocationsService;

    @Autowired
    private IWmsTasksRecordsService wmsTasksRecordsService;

    @Autowired
    private WmsInventoryTransByReceiving wmsInventoryTransByReceiving;


    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IWmsInventoryTransService iWmsInventoryTransService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReceiveTask(String orderId, String operator) {

        // 查询入库单
        WmsStockInOrders stockInOrder = wmsStockInOrdersService.getById(orderId);
        // 状态
        String status = stockInOrder.getStatus();
        // 审核通过的入库单方可创建收货任务
        if (!status.equals(WarehouseDictEnum.INBOUND_APPROVED.getCode())) {
            throw new JeecgBootException("入库单状态不是审核通过，不能创建收货任务");
        }

        // 查询入库单明细
        List<WmsStockInOrderItems> stockInOrderItemsList = wmsStockInOrderItemsService.selectByMainId(orderId);

        List<WmsTasks> wmsTasksList = new ArrayList<>();
        // 遍历入库单明细
        for (WmsStockInOrderItems stockInOrderItem : stockInOrderItemsList) {
            // 创建收货任务
            WmsTasks wmsTasks = new WmsTasks();
            // 任务号
            String taskNumber = generateTaskCode();
            wmsTasks.setTaskNumber(taskNumber);
            // 任务类型
            wmsTasks.setTaskType(WarehouseDictEnum.TASK_TYPE_RECEIVING.getCode());
            // 任务状态
            wmsTasks.setTaskStatus(WarehouseDictEnum.TASK_STATUS_CREATED.getCode());
            // 商品id
            wmsTasks.setProductId(stockInOrderItem.getProductId());
            // 数量
            wmsTasks.setQuantity(stockInOrderItem.getExpectedQuantity());
            // 完成数量为0
            wmsTasks.setCompletedQuantity(0);
            // 执行人
            wmsTasks.setOperator(operator);
            // 操作时间
            wmsTasks.setOperationTime(DateUtils.getDate());
            // 入库单id
            wmsTasks.setStockInOrderId(orderId);
            // 入库单明细id
            wmsTasks.setStockInOrderItemId(stockInOrderItem.getId());
            // 目的仓库
            wmsTasks.setTargetWarehouseId(stockInOrder.getWarehouseId());
            // 插入任务
            wmsTasksList.add(wmsTasks);
        }
        // 批量插入任务
        this.saveBatch(wmsTasksList);

        // 更新入库单的状态为收货中，以及设置总待收货数量
        stockInOrder.setStatus(WarehouseDictEnum.INBOUND_RECEIVING.getCode());
        boolean b = wmsStockInOrdersService.updateById(stockInOrder);
        if (!b) {
            throw new JeecgBootException("更新入库单状态失败");
        }
        // 更新入库单明细为收货中
        // sql update wms_stock_in_order_items set status = 'INBOUND_RECEIVING' where order_id = ?
        LambdaUpdateWrapper<WmsStockInOrderItems> set = new LambdaUpdateWrapper<>();
        set.eq(WmsStockInOrderItems::getOrderId, orderId)
                .set(WmsStockInOrderItems::getStatus, WarehouseDictEnum.INBOUND_DETAIL_RECEIVING.getCode());
        boolean b1 = wmsStockInOrderItemsService.update(set);
        if (!b1) {
            throw new JeecgBootException("更新入库单明细状态失败");
        }
    }

    /**
     * 生成任务编号
     * 规则：TSK+年月日+5位序号，序号使用redis自增序号实现
     */
    public String generateTaskCode() {
        //参考上边的代码实现
        String time = DateUtils.now().substring(0, 10).replace("-", "");
        String key = "tsk_number" + time;
        long incr = redisUtil.incr(key, 1);
        if (incr == 1) {
            //设置过期时间，设置24小时+10秒的目的是避免并发产生订单号重复
            redisUtil.expire(key, 24 * 60 * 60 + 10);
        }
        //将incr组成5位字符串
        String incrStr = String.format("%05d", incr);
        String taskNumber = "TSK" + time + incrStr;
        return taskNumber;
    }

    @Override
    public IPage<WmsTasks> list(WmsTasks wmsTasks, Integer pageNo, Integer pageSize) {
        Page<WmsTasks> page = PageHelper.startPage(pageNo, pageSize);
        List<WmsTasks> list = baseMapper.queryTaskList(wmsTasks);
        PageDTO<WmsTasks> wmsTasksPageDTO = new PageDTO<>();
        wmsTasksPageDTO.setRecords(list);
        wmsTasksPageDTO.setTotal(page.getTotal());
        wmsTasksPageDTO.setSize(page.getPageSize());
        wmsTasksPageDTO.setCurrent(page.getPageNum());
        wmsTasksPageDTO.setPages(page.getPages());
        return wmsTasksPageDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receive(WmsTasksRecords wmsTasksRecords) {
        // 执行任务，向任务表更新收货数量，如果收货完成更新状态为已完成，记录收货记录
        WmsTasks tasks = execute(wmsTasksRecords);
        // 更新入库单明细中的收货数量及不良品数量，当不良品数量加良品数量等于采购数量，更新状态为收货完成。
        wmsStockInOrderItemsService.updateReceivedStatus(tasks.getStockInOrderItemId());
        // 更新入库单中收货数量，如果所有明细的状态为收货完成，那么入库单的状态为完成。
        wmsStockInOrdersService.updateReceivedStatus(tasks.getStockInOrderId());
        // 增加库存
        WmsInventoryTransParam inventoryTransParam = new WmsInventoryTransParam();
        inventoryTransParam.setProductId(tasks.getProductId()); // 商品id
        inventoryTransParam.setExecQuantity(wmsTasksRecords.getExecQuantity()); // 执行数量
        inventoryTransParam.setTargetLocationCode(wmsTasksRecords.getTargetLocationCode()); // 目标储位编码
        inventoryTransParam.setTransactionType(WarehouseDictEnum.INVENTORY_RECEIVING.getCode()); // 库存变更类型
        inventoryTransParam.setWarehouseId(tasks.getTargetWarehouseId()); // 仓库id
        inventoryTransParam.setBatchNumber(wmsTasksRecords.getBatchNumber()); // 批次号
        inventoryTransParam.setOperator(tasks.getOperator()); // 执行人
        inventoryTransParam.setOperationTime(new Date());
        String inventoryAttribute = wmsTasksRecords.getInventoryAttribute(); // 库存属性
        // 如果库存属性是良品，那么设置isSellable为1，否则为0
        if (inventoryAttribute.equals(WarehouseDictEnum.INVENTORY_ATTRIBUTE_GOOD.getCode())) {
            inventoryTransParam.setIsSellable("1"); //可售
        }else {
            inventoryTransParam.setIsSellable("0"); //不可售
        }
        // 保质期
        inventoryTransParam.setExpiryDate(wmsTasksRecords.getExpiryDate());
        wmsInventoryTransByReceiving.transfer(inventoryTransParam);

        // 如果该入库单收货完成，那么创建上架任务
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsTasks execute(WmsTasksRecords wmsTasksRecords) {
        // 任务id
        String taskId = wmsTasksRecords.getTaskId();
        // 查询任务
        WmsTasks wmsTasks = this.getById(taskId);
        // 已完成数量
        Integer completedQuantity = wmsTasks.getCompletedQuantity();
        // 执行数量
        Integer executeQuantity = wmsTasksRecords.getExecQuantity();

        // 收获的总数量不能大于采购数量
        if (completedQuantity + executeQuantity > wmsTasks.getQuantity()) {
            throw new JeecgBootException("收货数量不能大于采购数量");
        }

        // 前端传入的库存属性
        String inventoryAttribute = wmsTasksRecords.getInventoryAttribute();
        // 前端传入的目标储位
        String targetStorageLocation = wmsTasksRecords.getTargetLocationCode();
        // 根据编码查询目标储位
        LambdaQueryWrapper<WmsStorageLocations> set = new LambdaQueryWrapper<>();
        set.eq(WmsStorageLocations::getLocationCode, targetStorageLocation);
        WmsStorageLocations wmsStorageLocations = wmsStorageLocationsService.getOne(set);
        // 是否可售 1：是 0：否
        String isSellable = wmsStorageLocations.getIsSellable();
        // 如果目标储位为可售并且商品为不良品，那么不能收货
        if ("1".equals(isSellable) && WarehouseDictEnum.RECEIVING_DEFECTIVE.getCode().equals(inventoryAttribute)) {
            throw new JeecgBootException("可售储位不能收货不良品");
        }
        // 添加收货记录
        wmsTasksRecords.setTaskId(taskId);
        // 任务类型
        wmsTasksRecords.setTaskType(wmsTasks.getTaskType());
        // 商品id
        wmsTasksRecords.setProductId(wmsTasks.getProductId());
        //仓库id
        wmsTasksRecords.setTargetWarehouseId(wmsTasks.getTargetWarehouseId());
        //入库单id
        wmsTasksRecords.setStockInOrderId(wmsTasks.getStockInOrderId());
        //入库单明细id
        wmsTasksRecords.setStockInOrderItemId(wmsTasks.getStockInOrderItemId());
        //任务id
        wmsTasksRecords.setTaskId(wmsTasks.getId());
        wmsTasksRecords.setTaskNumber(wmsTasks.getTaskNumber());
        //执行时间
        wmsTasksRecords.setOperationTime(new Date());
        //执行人
        wmsTasksRecords.setOperator(wmsTasks.getOperator());
        //添加执行任务记录
        boolean save = wmsTasksRecordsService.save(wmsTasksRecords);
        if (!save) {
            throw new JeecgBootException("添加执行任务记录失败");
        }
        // 向任务表增加收货数量，如果收货完成，记录收货，记录收货记录
        // sql更新 update wms_tasks set completed_quantity = completed_quantity + ?
        // where id = ? and completed_quantity <= executeQuantity
               LambdaUpdateWrapper<WmsTasks> set1 = new LambdaUpdateWrapper<>();
        set1.eq(WmsTasks::getId, taskId)
                .setSql("completed_quantity = completed_quantity + " + executeQuantity)
                .le(WmsTasks::getCompletedQuantity, wmsTasks.getQuantity() - executeQuantity);
        boolean update = this.update(set1);
        if (!update) {
            throw new JeecgBootException("更新任务表失败");
        }

        // 查询新的任务信息
        wmsTasks = this.getById(taskId);
        // 完成数量
        completedQuantity = wmsTasks.getCompletedQuantity();
        // 更新任务为已完成
        if (completedQuantity.equals(wmsTasks.getQuantity())) {
            wmsTasks.setTaskStatus(WarehouseDictEnum.TASK_STATUS_COMPLETED.getCode());
            boolean updateById = this.updateById(wmsTasks);
            if (!updateById) {
                throw new JeecgBootException("更新任务状态失败");
            }
        }
        return wmsTasks;
    }
}