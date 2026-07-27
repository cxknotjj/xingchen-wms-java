package org.jeecg.modules.wms.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.zhyd.oauth.utils.StringUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
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
 * 上架操作库存转换
 */
@Service
public class WmsInventoryTransByShelfing extends ServiceImpl<WmsInventoryTransMapper, WmsInventoryTrans> implements IWmsInventoryTransService {
    @Autowired
    private IWmsInventoryService wmsInventoryService;
    @Autowired
    private IWmsProductsService wmsProductsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(WmsInventoryTransParam inventoryTransParam) {
        // 关键字段非空判断：商品id、原储位、新储位不能为空
        if (StringUtils.isEmpty(inventoryTransParam.getProductId())
                || StringUtils.isEmpty(inventoryTransParam.getSourceLocationCode())
                || StringUtils.isEmpty(inventoryTransParam.getTargetLocationCode())) {
            throw new JeecgBootException("商品id，原储位，新储位不能为空");
        }

        // 上架只针对良品
        String isSellable = inventoryTransParam.getIsSellable();
        if (!"1".equals(isSellable)) {
            throw new JeecgBootException("上架只针对良品");
        }

        String productId = inventoryTransParam.getProductId();
        String sourceLocationCode = inventoryTransParam.getSourceLocationCode();
        String targetLocationCode = inventoryTransParam.getTargetLocationCode();
        Integer execQuantity = inventoryTransParam.getExecQuantity();
        String batchNumber = inventoryTransParam.getBatchNumber();

        // 查询商品信息
        WmsProducts products = wmsProductsService.getById(productId);
        if (products == null) {
            throw new JeecgBootException("商品不存在");
        }

        // ========== 1. 原储位处理：减少库存 ==========
        WmsInventory sourceInventory = wmsInventoryService.getInventoryByUniqueKey(productId, sourceLocationCode, batchNumber);
        if (sourceInventory == null) {
            throw new JeecgBootException("原储位库存不存在");
        }

        // 检查原储位库存是否足够
        if (sourceInventory.getStockQuantity() < execQuantity) {
            throw new JeecgBootException("原储位库存不足");
        }
        if (sourceInventory.getAvailableQuantity() < execQuantity) {
            throw new JeecgBootException("原储位可用库存不足");
        }

        // 更新原储位库存：在库数量减少，可用数量减少，分配数量不动
        LambdaUpdateWrapper<WmsInventory> sourceUpdate = new LambdaUpdateWrapper<>();
        sourceUpdate.eq(WmsInventory::getId, sourceInventory.getId())
                .setSql("stock_quantity = stock_quantity - {0}", execQuantity)
                .setSql("available_quantity = available_quantity - {0}", execQuantity);
        Boolean sourceResult = wmsInventoryService.update(null, sourceUpdate);
        if (!sourceResult) {
            throw new JeecgBootException("原储位库存更新失败");
        }

        // ========== 2. 新储位处理：增加库存 ==========
        WmsInventory targetInventory = wmsInventoryService.getInventoryByUniqueKey(productId, targetLocationCode, batchNumber);

        if (targetInventory == null) {
            // 新储位无库存，新增库存记录
            targetInventory = new WmsInventory();
            targetInventory.setOwnerId(products.getOwnerId());
            targetInventory.setProductId(productId);
            targetInventory.setLocationCode(targetLocationCode);
            targetInventory.setStockQuantity(execQuantity);
            targetInventory.setAllocatedQuantity(0);
            targetInventory.setAvailableQuantity(execQuantity); // 良品全部可用
            targetInventory.setBatchNumber(batchNumber);
            targetInventory.setStockInTime(inventoryTransParam.getOperationTime());
            targetInventory.setExpiryDate(inventoryTransParam.getExpiryDate());
            targetInventory.setIsSellable(WarehouseDictEnum.INVENTORY_ATTRIBUTE_GOOD.getCode());
            targetInventory.setWarehouseId(inventoryTransParam.getWarehouseId());
            wmsInventoryService.save(targetInventory);
        } else {
            // 新储位有库存，更新库存：在库数量增加，可用数量增加，分配数量不动
            LambdaUpdateWrapper<WmsInventory> targetUpdate = new LambdaUpdateWrapper<>();
            targetUpdate.eq(WmsInventory::getId, targetInventory.getId())
                    .setSql("stock_quantity = stock_quantity + {0}", execQuantity)
                    .setSql("available_quantity = available_quantity + {0}", execQuantity);
            Boolean targetResult = wmsInventoryService.update(null, targetUpdate);
            if (!targetResult) {
                throw new JeecgBootException("新储位库存更新失败");
            }
        }

        // ========== 3. 记录库存变更日志 ==========
        // 原储位出库记录
        WmsInventoryTrans sourceTrans = new WmsInventoryTrans();
        sourceTrans.setProductId(productId);
        sourceTrans.setLocationCode(sourceLocationCode);
        sourceTrans.setChangeQuantity(-execQuantity); // 负数表示减少
        sourceTrans.setTransactionType(inventoryTransParam.getTransactionType());
        sourceTrans.setRemarks("上架出库");
        sourceTrans.setTransactionTime(inventoryTransParam.getOperationTime());
        sourceTrans.setBatchNumber(batchNumber);
        save(sourceTrans);

        // 新储位入库记录
        WmsInventoryTrans targetTrans = new WmsInventoryTrans();
        targetTrans.setProductId(productId);
        targetTrans.setLocationCode(targetLocationCode);
        targetTrans.setChangeQuantity(execQuantity); // 正数表示增加
        targetTrans.setTransactionType(inventoryTransParam.getTransactionType());
        targetTrans.setRemarks("上架入库");
        targetTrans.setTransactionTime(inventoryTransParam.getOperationTime());
        targetTrans.setBatchNumber(batchNumber);
        save(targetTrans);
    }
}