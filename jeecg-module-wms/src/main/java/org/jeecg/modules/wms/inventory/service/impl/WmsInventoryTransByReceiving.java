package org.jeecg.modules.wms.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dm.jdbc.util.StringUtil;
import me.zhyd.oauth.utils.StringUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.wms.goods.entity.WmsProducts;
import org.jeecg.modules.wms.goods.service.IWmsProductsService;
import org.jeecg.modules.wms.inventory.entity.WmsInventory;
import org.jeecg.modules.wms.inventory.entity.WmsInventoryTrans;
import org.jeecg.modules.wms.inventory.mapper.WmsInventoryTransMapper;
import org.jeecg.modules.wms.inventory.service.IWmsInventoryService;
import org.jeecg.modules.wms.inventory.service.IWmsInventoryTransService;
import org.jeecg.modules.wms.inventory.vo.WmsInventoryTransParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 收货操作库存表
 */
@Service
public class WmsInventoryTransByReceiving extends ServiceImpl<WmsInventoryTransMapper, WmsInventoryTrans> implements IWmsInventoryTransService {
    @Autowired
    private IWmsInventoryService wmsInventoryService;
    @Autowired
    private IWmsProductsService wmsProductsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(WmsInventoryTransParam inventoryTransParam) {
        // 关键字段非空判断 商品id，储位不能为空
        if (StringUtils.isEmpty(inventoryTransParam.getProductId())
                || StringUtils.isEmpty(inventoryTransParam.getTargetLocationCode())) {
            throw new JeecgBootException("商品id，储位不能为空");
        }
        String isSellable = inventoryTransParam.getIsSellable();
        String productId = inventoryTransParam.getProductId();
        // 查询商品信息
        WmsProducts products = wmsProductsService.getById(productId);
        // 如果商品不存在，抛出异常
        if (products == null) {
            throw new JeecgBootException("商品不存在");
        }
        // 先查询库存，如果查到库存，那么就在原有库存基础上新增
        WmsInventory inventoryDb = wmsInventoryService.getInventoryByUniqueKey(inventoryTransParam.getProductId(), inventoryTransParam.getTargetLocationCode(), inventoryTransParam.getBatchNumber());
        // 如果查询不到库存，要向库存表新增一条
        if (inventoryDb == null) {
            // 新增库存
            inventoryDb = new WmsInventory();
            // 货主id
            inventoryDb.setOwnerId(products.getOwnerId());
            // 商品id
            inventoryDb.setProductId(inventoryTransParam.getProductId());
            // 储位编码
            inventoryDb.setLocationCode(inventoryTransParam.getTargetLocationCode());
            // 库存数量
            inventoryDb.setStockQuantity(inventoryTransParam.getExecQuantity());
            // 分配数量
            inventoryDb.setAllocatedQuantity(0);
            // 可用数量
            inventoryDb.setAvailableQuantity(isSellable.equals("1") ? inventoryTransParam.getExecQuantity() : 0);
            // 批号
            inventoryDb.setBatchNumber(inventoryTransParam.getBatchNumber());
            // 入库时间
            inventoryDb.setStockInTime(inventoryTransParam.getOperationTime());
            // 保质期
            inventoryDb.setExpiryDate(inventoryTransParam.getExpiryDate());
            // 是否可售
            inventoryDb.setIsSellable(isSellable);
            // 仓库id
            inventoryDb.setWarehouseId(inventoryTransParam.getWarehouseId());
            wmsInventoryService.save(inventoryDb);
        }else {
            // 更新库存
            LambdaUpdateWrapper<WmsInventory> update = new LambdaUpdateWrapper<>();
            update.eq(WmsInventory::getId, inventoryDb.getId())
                    .setSql("stock_quantity = stock_quantity + {0}", inventoryTransParam.getExecQuantity())
                       .setSql(isSellable.equals("1"),"available_quantity = available_quantity + {0}", inventoryTransParam.getExecQuantity());
            Boolean result = wmsInventoryService.update(null,update);
            if (!result) {
                throw new JeecgBootException("库存更新失败");
            }
        }
        // 向库存变更表插入一条记录
        WmsInventoryTrans InventoryTrans = new WmsInventoryTrans();
        InventoryTrans.setProductId(inventoryTransParam.getProductId()); // 商品id
        InventoryTrans.setLocationCode(inventoryTransParam.getTargetLocationCode()); // 目标储位编码
        InventoryTrans.setChangeQuantity(inventoryTransParam.getExecQuantity()); // 变更数量
        InventoryTrans.setTransactionType(inventoryTransParam.getTransactionType()); // 库存变更类型
        InventoryTrans.setRemarks(inventoryTransParam.getRemarks());
        InventoryTrans.setTransactionTime(inventoryTransParam.getOperationTime()); // 变更时间
        // 批号
        InventoryTrans.setBatchNumber(inventoryTransParam.getBatchNumber());
        save(InventoryTrans);
    }
}
