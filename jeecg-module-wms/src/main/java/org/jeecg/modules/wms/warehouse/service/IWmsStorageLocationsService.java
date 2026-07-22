package org.jeecg.modules.wms.warehouse.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.wms.warehouse.entity.WmsStorageLocations;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 储位表
 * @Author: jeecg-boot
 * @Date:   2026-07-21
 * @Version: V1.0
 */
public interface IWmsStorageLocationsService extends IService<WmsStorageLocations> {

    /**
     * 分页查询储位表
     * @param wmsStorageLocations
     * @param pageNo
     * @param pageSize
     * @return
     */
    IPage<WmsStorageLocations> queryList(WmsStorageLocations wmsStorageLocations, Integer pageNo, Integer pageSize);

    /**
     * 添加储位
     * @param wmsStorageLocations
     */
    void add(WmsStorageLocations wmsStorageLocations);

    /**
     * 启用储位表
     * @param id
     */
    void enable(String id);

    /**
     * 禁用储位表
     * @param id
     */
    void disable(String id);
}
