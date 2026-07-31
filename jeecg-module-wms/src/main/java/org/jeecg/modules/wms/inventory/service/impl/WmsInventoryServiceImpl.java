package org.jeecg.modules.wms.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.wms.inventory.entity.WmsInventory;
import org.jeecg.modules.wms.inventory.mapper.WmsInventoryMapper;
import org.jeecg.modules.wms.inventory.service.IWmsInventoryService;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrdersItems;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 库存表
 * @Author: jeecg-boot
 * @Date:   2025-08-30
 * @Version: V1.0
 */
@Service
public class WmsInventoryServiceImpl extends ServiceImpl<WmsInventoryMapper, WmsInventory> implements IWmsInventoryService {

    @Override
    public WmsInventory getInventoryByUniqueKey(String productId, String locationCode, String batchNumber) {
        LambdaQueryWrapper<WmsInventory> eq = new LambdaQueryWrapper<WmsInventory>()
                .eq(WmsInventory::getProductId, productId)
                .eq(WmsInventory::getLocationCode, locationCode)
                .eq(StringUtils.isNotEmpty(batchNumber),WmsInventory::getBatchNumber, batchNumber)
                .eq(StringUtils.isEmpty(batchNumber), WmsInventory::getBatchNumber, "");
        WmsInventory inventory = this.getOne(eq);

        return inventory;
    }

    @Override
    public IPage<WmsInventory> queryList(WmsInventory wmsInventory, Integer pageNo, Integer pageSize) {
        //开启分页查询,当执行查询时，插件进行相关的sql拦截进行分页操作，返回一个page对象
        Page<WmsInventory> page = PageHelper.startPage(pageNo, pageSize);
        List<WmsInventory> list = baseMapper.queryList(wmsInventory);
        PageDTO<WmsInventory> wmsInventoryPageDTO = new PageDTO<>();
        wmsInventoryPageDTO.setRecords(list);
        wmsInventoryPageDTO.setTotal(page.getTotal());
        wmsInventoryPageDTO.setSize(page.getPageSize());
        wmsInventoryPageDTO.setCurrent(page.getPageNum());
        wmsInventoryPageDTO.setPages(page.getPages());
        return wmsInventoryPageDTO;
    }

    @Override
    public List<WmsInventory> selectAvailableBySku(String warehouseId, WmsOutOrdersItems skuItem) {
        // sql = select * from wms_inventory where warehouse_id = ? and product_id = ? and is_sellable = "1" and available_quantity > 0 order by expiry_date, stock_in_time
        LambdaQueryWrapper<WmsInventory> eq = new LambdaQueryWrapper<WmsInventory>()
                .eq(WmsInventory::getWarehouseId, warehouseId)
                .eq(WmsInventory::getProductId, skuItem.getSkuId())
                .eq(WmsInventory::getIsSellable, "1")
                .eq(StringUtils.isNotEmpty(skuItem.getBatchNumber()),WmsInventory::getBatchNumber, skuItem.getBatchNumber())
                .gt(WmsInventory::getAvailableQuantity, 0) // 可用库存大于0
                .orderByAsc(WmsInventory::getExpiryDate)   // 按到期时间排序
                .orderByAsc(WmsInventory::getStockInTime);  // 如果到期时间相同，按照入库时间升序
        List<WmsInventory> list = this.list(eq);
        return list;
    }
}
