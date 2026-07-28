package org.jeecg.modules.wms.inventory.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.wms.inventory.entity.WmsInventory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 库存表
 * @Author: jeecg-boot
 * @Date:   2025-08-30
 * @Version: V1.0
 */
public interface IWmsInventoryService extends IService<WmsInventory> {
    /**
     * 查询唯一库存
     */
    WmsInventory getInventoryByUniqueKey(String productId, String locationCode, String batchNumber);

    /**
     * 综合查询库存列表
     */
    public IPage<WmsInventory> queryList(WmsInventory wmsInventory, Integer pageNo, Integer pageSize);
}
