package org.jeecg.modules.wms.wave.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.goods.entity.WmsProducts;
import org.jeecg.modules.wms.goods.service.IWmsProductsService;
import org.jeecg.modules.wms.inventory.service.IWmsInventoryService;
//import org.jeecg.modules.wms.inventory.service.impl.WmsInventoryTransByPick;
import org.jeecg.modules.wms.inventory.service.impl.WmsInventoryTransByPick;
import org.jeecg.modules.wms.inventory.vo.WmsInventoryTransParam;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrders;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrdersAllocation;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrdersItems;
import org.jeecg.modules.wms.outorder.mapper.WmsOutOrdersAllocationMapper;
import org.jeecg.modules.wms.outorder.service.IWmsOutOrdersAllocationService;
import org.jeecg.modules.wms.outorder.service.IWmsOutOrdersItemsService;
import org.jeecg.modules.wms.outorder.service.IWmsOutOrdersService;
import org.jeecg.modules.wms.warehouse.entity.WmsStorageLocations;
import org.jeecg.modules.wms.warehouse.entity.WmsStorageZones;
import org.jeecg.modules.wms.warehouse.service.IWmsStorageLocationsService;
import org.jeecg.modules.wms.warehouse.service.IWmsStorageZonesService;
import org.jeecg.modules.wms.wave.entity.WmsShortageRegistration;
import org.jeecg.modules.wms.wave.entity.WmsWaveMaster;
import org.jeecg.modules.wms.wave.entity.WmsWaveSkuSummary;
import org.jeecg.modules.wms.wave.mapper.WmsWaveMasterMapper;
import org.jeecg.modules.wms.wave.service.IPickingTasksService;
import org.jeecg.modules.wms.wave.service.IWmsShortageRegistrationService;
import org.jeecg.modules.wms.wave.service.IWmsWaveMasterService;
import org.jeecg.modules.wms.wave.service.IWmsWaveSkuSummaryService;
import org.jeecg.modules.wms.wmstask.entity.WmsTasks;
import org.jeecg.modules.wms.wmstask.entity.WmsTasksRecords;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksRecordsService;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksService;
import org.jeecg.modules.wms.wmstask.service.impl.WmsTasksRecordsServiceImpl;
import org.jeecg.modules.wms.wmstask.service.impl.WmsTasksServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Mr.M
 * @version 1.0
 * @description 拣货服务类
 * @date 2025/6/6 8:31
 */
@Service
public class PickingTasksServiceImpl implements IPickingTasksService {
    @Autowired
    private IWmsTasksService wmsTasksService;
    @Autowired
    private IWmsWaveMasterService wmsWaveMasterService;
    @Autowired
    private IWmsOutOrdersService wmsOutOrdersService;

    @Autowired
    private IWmsProductsService wmsProductsService;

    @Autowired
    private IWmsOutOrdersItemsService wmsOutOrdersItemsService;

    @Autowired
    private WmsOutOrdersAllocationMapper wmsOutOrdersAllocationMapper;

    @Autowired
    private IWmsOutOrdersAllocationService wmsOutOrdersAllocationService;

    @Autowired
    private IWmsWaveSkuSummaryService wmsWaveSkuSummaryService;
    @Autowired
    private WmsInventoryTransByPick inventoryTransByPick;


//    @Autowired
//    private WmsInventoryTransByPick inventoryTransByPick;

    @Autowired
    private IWmsTasksRecordsService wmsTasksRecordsService;

    @Autowired
    private IWmsShortageRegistrationService wmsShortageRegistrationService;

    @Autowired
    private IWmsStorageLocationsService wmsStorageLocationsService;

    //储区service
    @Autowired
    private IWmsStorageZonesService wmsStorageZonesService;

    /**
     * 生成拣货任务
     *
     * @param waveIds 波次id集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPickTask(List<String> waveIds) {
        //遍历波次id集合，调用创建拣货任务方法
        for (String waveId : waveIds) {
            createPickTask(waveId);
        }
    }

    /**
     * 根据波次统计出库单的商品分配数量,添加到波次拣货明细表
     *
     * @param waveId 波次id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void createPickTask(String waveId) {
        // 查询波次
        WmsWaveMaster wmsWaveMaster = wmsWaveMasterService.getById(waveId);
        if (ObjectUtil.isEmpty(wmsWaveMaster)) {
            return;
        }
        IPage<WmsOutOrdersAllocation> wmsOutOrdersAllocationIPage = wmsOutOrdersAllocationService.selectAllocatedQuantityByWaveId(waveId, 1, 100);
        if (wmsOutOrdersAllocationIPage.getRecords().size() == 0) {
            return;
        }
        int i = 1;
        while (true) {
            List<WmsOutOrdersAllocation> records = wmsOutOrdersAllocationIPage.getRecords();
            // 根据库存分配统计的结果，向wms_wave_sku_summary表添加数据
            List<WmsWaveSkuSummary> waveSkuSummaryList = records.stream().map(wmsOutOrdersAllocation -> {
                WmsWaveSkuSummary wmsWaveSkuSummary = new WmsWaveSkuSummary();
                wmsWaveSkuSummary.setWaveId(waveId);
                wmsWaveSkuSummary.setSkuId(wmsOutOrdersAllocation.getSkuId());
                wmsWaveSkuSummary.setLocationCode(wmsOutOrdersAllocation.getLocationCode());
                wmsWaveSkuSummary.setBatchNumber(wmsOutOrdersAllocation.getBatchNumber());
                wmsWaveSkuSummary.setAllocatedQuantity(wmsOutOrdersAllocation.getAllocatedQuantity());
                wmsWaveSkuSummary.setOwnerId(wmsOutOrdersAllocation.getOwnerId());
                wmsWaveSkuSummary.setContainerCode(wmsOutOrdersAllocation.getContainerCode());
                wmsWaveSkuSummary.setProductBarcode(wmsOutOrdersAllocation.getProductBarcode());
                return wmsWaveSkuSummary;
            }).collect(Collectors.toList());
            wmsWaveSkuSummaryService.saveBatch(waveSkuSummaryList);

            // 向任务表添加拣货任务
            // 将wmsWaveSkuSummaryList转换为wmsTasks列表
            List<WmsTasks> wmsTasksList = waveSkuSummaryList.stream().map(wmsWaveSkuSummary -> {
                WmsTasks wmsTasks = new WmsTasks();
                wmsTasks.setTaskType(WarehouseDictEnum.TASK_TYPE_PICKING.getCode());
                //任务状态，已创建
                wmsTasks.setTaskStatus(WarehouseDictEnum.TASK_STATUS_CREATED.getCode());//任务创建时间
                wmsTasks.setCreateTime(new Date());
                //任务号
                wmsTasks.setTaskNumber(wmsTasksService.generateTaskCode());
                //波次id
                wmsTasks.setWaveOrderId(waveId);
                //商品id
                wmsTasks.setProductId(wmsWaveSkuSummary.getSkuId());
                //商品待拣货数量
                wmsTasks.setQuantity(wmsWaveSkuSummary.getAllocatedQuantity());
                //来源储位编码
                wmsTasks.setSourceLocationCode(wmsWaveSkuSummary.getLocationCode());
                //来源容器编码
                wmsTasks.setSourceContainerCode(wmsWaveSkuSummary.getContainerCode());
                //来源仓库
                wmsTasks.setSourceWarehouseId(wmsWaveMaster.getWarehouseId());
                //目的仓库
                wmsTasks.setTargetWarehouseId(wmsWaveMaster.getWarehouseId());
                //商品批次号
                wmsTasks.setBatchNumber(wmsWaveSkuSummary.getBatchNumber());
                //目的储位编码
                wmsTasks.setTargetLocationCode("");
                //波次拣货明细id
                wmsTasks.setWaveSkuSummaryId(wmsWaveSkuSummary.getId());
                //完成数量为0
                wmsTasks.setCompletedQuantity(0);
                return wmsTasks;
            }).collect(Collectors.toList());
            // 向任务表添加拣货任务
            wmsTasksService.saveBatch(wmsTasksList);
            wmsOutOrdersAllocationIPage = wmsOutOrdersAllocationService.selectAllocatedQuantityByWaveId(waveId, ++i, 100);
            if (wmsOutOrdersAllocationIPage.getRecords().size() == 0) {
                break;
            }
            // 更新该波次下出库单状态为拣货中
            LambdaUpdateWrapper<WmsOutOrders> updateWrapper = new LambdaUpdateWrapper<WmsOutOrders>()
                    .eq(WmsOutOrders::getWaveId, waveId)
                    .eq(WmsOutOrders::getStatus, WarehouseDictEnum.OUTBOUND_ALLOCATED.getCode())
                    .set(WmsOutOrders::getStatus, WarehouseDictEnum.OUTBOUND_PICKING.getCode());
            wmsOutOrdersService.update(null, updateWrapper);

            // 根据波次id查询出库单id
            List<WmsOutOrders> wmsOutOrders = wmsOutOrdersService.selectByWaveId(waveId);
            List<String> orderIds = wmsOutOrders.stream().map(WmsOutOrders::getId).collect(Collectors.toList());

            // 更新该波次下出库单明细状态为拣货中
            // sql = update wms_out_orders_items set status = 'PICKING'
            // where order_id in (select id from wms_out_orders where wave_id = #{waveId}) and status = 'ALLOCATED'
            LambdaUpdateWrapper<WmsOutOrdersItems> updateWrapper2 = new LambdaUpdateWrapper<WmsOutOrdersItems>()
                    .in(WmsOutOrdersItems::getOrderId, orderIds)
                    .eq(WmsOutOrdersItems::getStatus, WarehouseDictEnum.OUTBOUND_DETAIL_ALLOCATED.getCode())
                    .set(WmsOutOrdersItems::getStatus, WarehouseDictEnum.OUTBOUND_PICKING.getCode());
            wmsOutOrdersItemsService.update(null, updateWrapper2);
        }
    }

    /**
     * 完成拣货
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completePickTask(List<String> waveIds) {
        for (String waveId : waveIds) {
            completePickTask(waveId);
        }
    }

    /**
     * 完成拣货
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completePickTask(String waveId) {
        //查询该波次下的拣货任务列表
        LambdaQueryWrapper<WmsTasks> eq = new LambdaQueryWrapper<WmsTasks>().eq(WmsTasks::getWaveOrderId, waveId);
        List<WmsTasks> wmsTasks = wmsTasksService.list(eq);
        //只要存在一个未拣货完成则抛出异常
        boolean b = wmsTasks.stream().anyMatch(wmsTask -> !WarehouseDictEnum.TASK_STATUS_COMPLETED.getCode().equals(wmsTask.getTaskStatus()));
        if (b) {
            throw new RuntimeException("波次下存在未完成拣货任务");
        }
        //更新波次下拣货明细表的状态为已拣货
        wmsWaveSkuSummaryService.updatePickedStatus(waveId);
        //更新波次主表的拣货状态，如果波次下拣货明细的状态为已拣货，则更新波次主表的拣货状态为拣货完成
        wmsWaveMasterService.updatePickStatus(waveId);
    }

    /**
     * 拣货校验：已拣货数量+缺货数量不能大于计划数量
     *
     * @param pickTaskId 拣货任务id
     */
    @Override
    public void checkPickedQuantity(String pickTaskId) {
        //根据任务id查询任务
        WmsTasks wmsTasks = wmsTasksService.getById(pickTaskId);
        //根据任务id查询任务记录表
        List<WmsTasksRecords> wmsTasksRecords1 = wmsTasksRecordsService.getBaseMapper().selectList(new LambdaQueryWrapper<WmsTasksRecords>().eq(WmsTasksRecords::getTaskId, pickTaskId));
        //获取已拣货总数量
        Integer pickedQuantity = wmsTasksRecords1.stream().mapToInt(WmsTasksRecords::getExecQuantity).sum();
        //根据任务id查询缺货记录
        List<WmsShortageRegistration> wmsShortageRegistrations = wmsShortageRegistrationService.getBaseMapper().selectList(new LambdaQueryWrapper<WmsShortageRegistration>().eq(WmsShortageRegistration::getTaskId, pickTaskId));
        //获取缺货总数量
        Integer shortageQuantity = wmsShortageRegistrations.stream().mapToInt(WmsShortageRegistration::getShortageQuantity).sum();
        if (pickedQuantity + shortageQuantity > wmsTasks.getQuantity()) {
            throw new JeecgBootException("已拣货数量+缺货数量不能大于计划拣货数量");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pick(WmsTasksRecords wmsTasksRecords) {
        //执行任务
        WmsTasks wmsTasks = wmsTasksService.execute(wmsTasksRecords);
        //校验缺货数量+拣货数量不能大于计划数量
        checkPickedQuantity(wmsTasksRecords.getTaskId());
        //执行数量
        Integer execQuantity = wmsTasksRecords.getExecQuantity();

        //计划数量
        Integer quantity = wmsTasks.getQuantity();

        //波次拣货明细id
        String waveSkuSummaryId = wmsTasks.getWaveSkuSummaryId();
        //根据波次拣货明细id更新波次拣货数量，包括 波次拣货明细中的拣货数量和出库单分配明细中拣货数量
        updatePickedQuantityByWaveSkuSummaryId(waveSkuSummaryId, quantity, execQuantity);
        //更新波次主表的拣货状态，如果波次下拣货明细的状态为已拣货，则更新波次主表的拣货状态为拣货完成
        wmsWaveMasterService.updatePickStatus(wmsTasks.getWaveOrderId());
        //向库存表中添加库存记录
        WmsInventoryTransParam inventoryTransParam = new WmsInventoryTransParam();
        inventoryTransParam.setProductId(wmsTasksRecords.getProductId());
        inventoryTransParam.setExecQuantity(wmsTasksRecords.getExecQuantity());
        inventoryTransParam.setSourceLocationCode(wmsTasksRecords.getSourceLocationCode());
        inventoryTransParam.setTargetLocationCode(wmsTasksRecords.getTargetLocationCode());
        inventoryTransParam.setWarehouseId(wmsTasksRecords.getTargetWarehouseId());
        inventoryTransParam.setBatchNumber(wmsTasksRecords.getBatchNumber());
        inventoryTransParam.setExpiryDate(wmsTasksRecords.getExpiryDate());
        inventoryTransParam.setTransactionType(WarehouseDictEnum.INVENTORY_PICKING.getCode());
        //上架时间
        inventoryTransParam.setOperationTime(wmsTasksRecords.getOperationTime());
        inventoryTransByPick.transfer(inventoryTransParam);
    }

    /**
     * 根据波次拣货明细id查询出库单分配明细并更新拣货数量
     *
     * @param waveSkuSummaryId 波次拣货明细id
     * @param quantity         计划拣货数量
     * @param pickedQuantity   拣货数量
     */
    public void updatePickedQuantityByWaveSkuSummaryId(String waveSkuSummaryId, Integer quantity, Integer pickedQuantity) {
        //更新波次拣货明细的拣货数量
        //sql="update wms_wave_sku_summary set picked_quantity = picked_quantity+"+pickedQuantity+" where id = '"+waveSkuSummaryId+"' and picked_quantity <="+quantity-pickedQuantity;
        LambdaUpdateWrapper<WmsWaveSkuSummary> waveSkuSummaryLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        waveSkuSummaryLambdaUpdateWrapper.setSql("picked_quantity = picked_quantity+" + pickedQuantity).eq(WmsWaveSkuSummary::getId, waveSkuSummaryId).le(WmsWaveSkuSummary::getPickedQuantity, quantity - pickedQuantity);
        int update2 = wmsWaveSkuSummaryService.getBaseMapper().update(null, waveSkuSummaryLambdaUpdateWrapper);
        if (update2 <= 0) {
            throw new JeecgBootException("波次拣货明细的拣货数量不能大于分配数量!");
        }
        //查询波次拣货明细
        WmsWaveSkuSummary wmsWaveSkuSummary = wmsWaveSkuSummaryService.getBaseMapper().selectById(waveSkuSummaryId);
        //如果拣货数量等于计划拣货数量，则更新波次拣货明细的拣货状态为已拣货
        if (wmsWaveSkuSummary.getPickedQuantity().equals(quantity)) {
            wmsWaveSkuSummary.setStatus(WarehouseDictEnum.WAVESKU_PICKED.getCode());
            wmsWaveSkuSummaryService.updateById(wmsWaveSkuSummary);
        }
    }

    /**
     * 完成分拣
     *
     * @param wmsOutOrdersItemsList 出库单明细
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void completeSorting(List<WmsOutOrdersItems> wmsOutOrdersItemsList) {
        //转成数组
//        String[] outOrderItemIdArray = outOrderItemIds.split(",");
        //出库单idList
        List<String> outOrderIds = new ArrayList<>();
        //波次id List
        List<String> waveIds = new ArrayList<>();
        //遍历出库单明细id
        for (WmsOutOrdersItems items : wmsOutOrdersItemsList) {
            //根据出库单明细id查询出库单明细
            WmsOutOrdersItems wmsOutOrdersItems = wmsOutOrdersItemsService.getById(items.getId());
            if (wmsOutOrdersItems == null) {
                throw new RuntimeException("未找到对应的出库单明细,id:" + items.getId());
            }
            //商品id
            String skuId = wmsOutOrdersItems.getSkuId();
            //拣货数量,页面传入
            Integer pickedQuantity = items.getPickedQuantity();
            //商品信息
            WmsProducts wmsProducts = wmsProductsService.getById(skuId);
            //如果状态为分拣完成状态，则不允许分拣
            if (WarehouseDictEnum.OUTBOUND_DETAIL_PICKED.getCode().equals(wmsOutOrdersItems.getStatus())) {
                throw new RuntimeException("出库单商品" + wmsProducts.getProductName() + "为拣货完成状态,不允许分拣");
            }
            //根据出库单id查询出库单
            WmsOutOrders wmsOutOrders = wmsOutOrdersService.getById(wmsOutOrdersItems.getOrderId());
            if (wmsOutOrders == null) {
                throw new RuntimeException("未找到对应的出库单,id:" + wmsOutOrdersItems.getOrderId());
            }
            outOrderIds.add(wmsOutOrders.getId());
            //波次id
            String waveId = wmsOutOrders.getWaveId();
            waveIds.add(waveId);
            //向波次表增加分拣数量
            wmsWaveMasterService.addSortingQuantity(waveId, pickedQuantity);
            //更新出库单明细的拣货数量及状态更新为分拣完成
            wmsOutOrdersItemsService.updatePickedQuantityAndStatus(items.getId(), pickedQuantity);
        }
        //更新出库单状态为分拣完成
        wmsOutOrdersService.updatePickStatus(outOrderIds);
        //更新该波次完成分拣订单的数量
        wmsWaveMasterService.updateCompleteSortingOrderQuantity(waveIds);

    }


}
