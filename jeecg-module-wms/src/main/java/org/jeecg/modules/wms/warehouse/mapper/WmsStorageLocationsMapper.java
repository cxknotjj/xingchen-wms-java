package org.jeecg.modules.wms.warehouse.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.wms.warehouse.entity.WmsStorageLocations;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 储位表
 * @Author: jeecg-boot
 * @Date:   2026-07-21
 * @Version: V1.0
 */
public interface WmsStorageLocationsMapper extends BaseMapper<WmsStorageLocations> {

    /**
     * 分页列表查询
     * @param wmsStorageLocations
     * @return
     */
    List<WmsStorageLocations> queryList(WmsStorageLocations wmsStorageLocations);
}
