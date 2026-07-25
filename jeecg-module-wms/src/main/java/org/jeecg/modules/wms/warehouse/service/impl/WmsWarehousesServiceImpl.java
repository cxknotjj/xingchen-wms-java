package org.jeecg.modules.wms.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.warehouse.entity.WmsWarehouses;
import org.jeecg.modules.wms.warehouse.mapper.WmsWarehousesMapper;
import org.jeecg.modules.wms.warehouse.service.IWmsWarehousesService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 仓库表
 * @Author: jeecg-boot
 * @Date:   2025-09-02
 * @Version: V1.0
 */
@Service
public class WmsWarehousesServiceImpl extends ServiceImpl<WmsWarehousesMapper, WmsWarehouses> implements IWmsWarehousesService {

    @Override
    public void add(WmsWarehouses wmsWarehouses) {
        //增加对仓库代码的唯一性校验
        //根据仓库代码查询仓库表
        //sql select count(*) from wms_warehouses where warehouse_code = ?
        LambdaQueryWrapper<WmsWarehouses> eq = new LambdaQueryWrapper<WmsWarehouses>()
                .eq(WmsWarehouses::getWarehouseCode, wmsWarehouses.getWarehouseCode());
        long count = this.count(eq);
        if (count > 0) {
            throw new RuntimeException("仓库代码已存在");
        }

        save(wmsWarehouses);
    }

    @Override
    public void edit(WmsWarehouses wmsWarehouses) {
        //增加对仓库代码的唯一性校验
        //sql select count(*) from wms_warehouses where warehouse_code = ? and id != ?
        LambdaQueryWrapper<WmsWarehouses> eq = new LambdaQueryWrapper<WmsWarehouses>()
                .eq(WmsWarehouses::getWarehouseCode, wmsWarehouses.getWarehouseCode())
                .ne(WmsWarehouses::getId, wmsWarehouses.getId());
        long count = this.count(eq);
        if (count > 0) {
            throw new RuntimeException("仓库代码已存在");
        }

        updateById(wmsWarehouses);
    }

    @Override
    public void enable(String id) {
        //查询仓库
        WmsWarehouses wmsWarehouses = getById(id);
        //如果仓库不存在
        if (wmsWarehouses == null) {
            throw new RuntimeException("仓库不存在");
        }
        String status = wmsWarehouses.getStatus();
        //仓库状态为“创建”或禁用时方可启用
        if (!WarehouseDictEnum.STATUS_CREATED.getCode().equals(status)
                && !WarehouseDictEnum.STATUS_INACTIVE.getCode().equals(status)) {
            throw new RuntimeException("仓库状态为“创建”或禁用时方可启用");
        }
        //设置状态为启用
        wmsWarehouses.setStatus(WarehouseDictEnum.STATUS_ACTIVE.getCode());
        updateById(wmsWarehouses);
    }

    @Override
    public void disable(String id) {
        //查询仓库
        WmsWarehouses wmsWarehouses = getById(id);
        //如果仓库不存在
        if (wmsWarehouses == null) {
            throw new RuntimeException("仓库不存在");
        }
        String status = wmsWarehouses.getStatus();
        //仓库状态为“启用”时方可禁用
        if (!WarehouseDictEnum.STATUS_ACTIVE.getCode().equals(status)) {
            throw new RuntimeException("仓库状态为“启用”时方可禁用");
        }

        //设置状态为启用
        wmsWarehouses.setStatus(WarehouseDictEnum.STATUS_INACTIVE.getCode());
        updateById(wmsWarehouses);
    }
}
