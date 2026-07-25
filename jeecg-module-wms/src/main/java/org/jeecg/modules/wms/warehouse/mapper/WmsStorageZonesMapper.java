package org.jeecg.modules.wms.warehouse.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.wms.warehouse.entity.WmsStorageZones;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 储区表
 * @Author: jeecg-boot
 * @Date:   2025-09-03
 * @Version: V1.0
 */
public interface WmsStorageZonesMapper extends BaseMapper<WmsStorageZones> {
    /**
     * 查询库区信息
     *
     */
    List<WmsStorageZones> queryList(WmsStorageZones wmsStorageZones);
}
