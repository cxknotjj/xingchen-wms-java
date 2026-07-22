package org.jeecg.modules.wms.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.warehouse.entity.WmsWarehouses;
import org.jeecg.modules.wms.warehouse.mapper.WmsWarehousesMapper;
import org.jeecg.modules.wms.warehouse.service.IWmsWarehousesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 仓库表
 * @Author: jeecg-boot
 * @Date:   2026-07-20
 * @Version: V1.0
 */
@Service
public class WmsWarehousesServiceImpl extends ServiceImpl<WmsWarehousesMapper, WmsWarehouses> implements IWmsWarehousesService {

    @Autowired
    private WmsWarehousesMapper wmsWarehousesMapper;

    @Override
    public void add(WmsWarehouses wmsWarehouses) {
        // 增加对代码仓库的唯一性校验
        LambdaQueryWrapper<WmsWarehouses> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(WmsWarehouses::getWarehouseCode, wmsWarehouses.getWarehouseCode());
        long count = this.count(lambdaQueryWrapper);
        if(count>0) {
            throw new JeecgBootException("仓库代码已存在");
        }
        save(wmsWarehouses);
    }

    /**
     * 修改仓库
     * @param wmsWarehouses
     */
    @Override
    public void edit(WmsWarehouses wmsWarehouses) {
        // 增加对代码仓库的唯一性校验
        LambdaQueryWrapper<WmsWarehouses> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(WmsWarehouses::getWarehouseCode, wmsWarehouses.getWarehouseCode());
        lambdaQueryWrapper.ne(WmsWarehouses::getId, wmsWarehouses.getId());
        long count = this.count(lambdaQueryWrapper);
        if(count>0) {
            throw new JeecgBootException("仓库代码已存在");
        }
        updateById(wmsWarehouses);
    }

    @Override
    public void enable(String id) {
        // 查询仓库
        WmsWarehouses wmsWarehouses = this.getById(id);
        // 仓库不存在
        if(wmsWarehouses==null) {
            throw new JeecgBootException("仓库不存在");
        }
        String status = wmsWarehouses.getStatus();
        // 仓库状态为创建或者禁用时方可启用
        if(!status.equals(WarehouseDictEnum.STATUS_CREATED.getCode()) &&
                !status.equals(WarehouseDictEnum.STATUS_INACTIVE.getCode())) {
            throw new JeecgBootException("仓库状态错误，只能启用创建或者禁用状态的仓库");
        }
        // 启用仓库
        wmsWarehouses.setStatus(WarehouseDictEnum.STATUS_ACTIVE.getCode());
        updateById(wmsWarehouses);
    }

    @Override
    public void disable(String id) {
        // 查询仓库
        WmsWarehouses wmsWarehouses = this.getById(id);
        // 仓库不存在
        if(wmsWarehouses==null) {
            throw new JeecgBootException("仓库不存在");
        }
        String status = wmsWarehouses.getStatus();
        // 仓库状态为启用时方可禁用
        if(!status.equals(WarehouseDictEnum.STATUS_ACTIVE.getCode())) {
            throw new JeecgBootException("仓库状态错误，只能禁用启用状态的仓库");
        }
        // 禁用仓库
        wmsWarehouses.setStatus(WarehouseDictEnum.STATUS_INACTIVE.getCode());
        updateById(wmsWarehouses);
    }
}
