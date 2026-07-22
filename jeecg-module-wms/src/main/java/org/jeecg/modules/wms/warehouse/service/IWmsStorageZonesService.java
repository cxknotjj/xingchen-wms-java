package org.jeecg.modules.wms.warehouse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.wms.warehouse.entity.WmsStorageZones;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 储区表
 * @Author: jeecg-boot
 * @Date:   2026-07-21
 * @Version: V1.0
 */
public interface IWmsStorageZonesService extends IService<WmsStorageZones> {

    /**
     * 分页查询储区列表
     * @param wmsStorageZones
     * @param pageNo
     * @param pageSize
     * @return
     */
    IPage<WmsStorageZones> queryList(WmsStorageZones wmsStorageZones, Integer pageNo, Integer pageSize);
}
