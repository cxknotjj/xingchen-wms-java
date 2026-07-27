package org.jeecg.modules.wms.inventory.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.wms.inventory.entity.WmsInventory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 库存表
 * @Author: jeecg-boot
 * @Date:   2026-07-26
 * @Version: V1.0
 */
public interface WmsInventoryMapper extends BaseMapper<WmsInventory> {

    /**
     * 查询库存列表
     * @param wmsInventory 查询条件
     * @return 库存列表
     */
    List<WmsInventory> queryInventoryList(@Param("wmsInventory") WmsInventory wmsInventory);

}