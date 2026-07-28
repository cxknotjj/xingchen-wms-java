package org.jeecg.modules.wms.inventory.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.wms.inventory.entity.WmsInventory;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.wms.inventory.excel.WmsInventoryImport;

import java.util.List;

/**
 * @Description: 库存表
 * @Author: jeecg-boot
 * @Date:   2026-07-26
 * @Version: V1.0
 */
public interface IWmsInventoryService extends IService<WmsInventory> {
    /**
     根据唯一键获取库存
     *@param productId
     @param locationCode
     @param batchNumber
     @return
     */
    public WmsInventory getInventoryByUniqueKey(String productId, String locationCode,String batchNumber);

    /**
     * 分页列表查询
     * @param wmsInventory
     * @param pageNo
     * @param pageSize
     * @return
     */
    IPage<WmsInventory> queryInventoryList(WmsInventory wmsInventory, Integer pageNo, Integer pageSize);
    /**
     * 导入库存
     * @param
     */
    void importInventory(List<WmsInventoryImport> importInventoryList);
}
