package org.jeecg.modules.wms.warehouse.service.impl;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.warehouse.entity.WmsStorageLocations;
import org.jeecg.modules.wms.warehouse.entity.WmsStorageZones;
import org.jeecg.modules.wms.warehouse.entity.WmsWarehouses;
import org.jeecg.modules.wms.warehouse.mapper.WmsStorageLocationsMapper;
import org.jeecg.modules.wms.warehouse.mapper.WmsStorageZonesMapper;
import org.jeecg.modules.wms.warehouse.mapper.WmsWarehousesMapper;
import org.jeecg.modules.wms.warehouse.service.IWmsStorageLocationsService;
import org.jeecg.modules.wms.warehouse.service.IWmsStorageZonesService;
import org.jeecg.modules.wms.warehouse.service.IWmsWarehousesService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 储位表
 * @Author: jeecg-boot
 * @Date:   2026-07-21
 * @Version: V1.0
 */
@Service
public class WmsStorageLocationsServiceImpl extends ServiceImpl<WmsStorageLocationsMapper, WmsStorageLocations> implements IWmsStorageLocationsService {

    @Autowired
    private WmsWarehousesMapper wmsWarehousesMapper;

    @Autowired
    private WmsStorageZonesMapper wmsStorageZonesMapper;

    @Override
    public IPage<WmsStorageLocations> queryList(WmsStorageLocations wmsStorageLocations, Integer pageNo, Integer pageSize) {
        // 将分页参数设置到Threadlocal中
        Page<WmsStorageLocations> page = PageHelper.startPage(pageNo, pageSize);
        // 调用mapper查询
        List<WmsStorageLocations> wmsStorageLocations1 = baseMapper.queryList(wmsStorageLocations);
        PageDTO<WmsStorageLocations> pageDTO = new PageDTO<>();
        // 当前页记录数
        pageDTO.setRecords(wmsStorageLocations1);
        // 总记录数
        pageDTO.setTotal(page.getTotal());
        //
        pageDTO.setCurrent(page.getPageNum());
        // 每页记录数
        pageDTO.setSize(page.getPageSize());
        // 总页数
        pageDTO.setPages(page.getPages());
        return pageDTO;
    }

    @Override
    public void add(WmsStorageLocations wmsStorageLocations) {
        // 创建一个新的storagelocation
        WmsStorageLocations storageLocation = new WmsStorageLocations();

        // 赋值，将wmsStorageLocations赋值给storagelocation
        BeanUtils.copyProperties(wmsStorageLocations, storageLocation);
        // 生成储位编码
        String code = generateLocationCode(storageLocation);
        storageLocation.setLocationCode(code);
        storageLocation.setCreateTime(DateTime.now());

        // 判断是否存在此储位
        WmsStorageLocations existingLocation = baseMapper.selectOne(new QueryWrapper<WmsStorageLocations>()
                .eq("location_code", code));
        if (existingLocation != null) {
            throw new JeecgBootException("储位编码已存在");
        }
        // 插入数据
        baseMapper.insert(storageLocation);

    }

    @Override
    public void enable(String id) {
        // 查询仓库
        WmsStorageLocations storageLocation = this.getById(id);
        // 仓库不存在
        if(storageLocation==null) {
            throw new JeecgBootException("储位不存在");
        }
        String status = storageLocation.getStatus();
        // 储位状态为创建或者禁用时方可启用
        if(!status.equals(WarehouseDictEnum.STATUS_CREATED.getCode()) &&
                !status.equals(WarehouseDictEnum.STATUS_INACTIVE.getCode())) {
            throw new JeecgBootException("储位状态错误，只能启用创建或者禁用状态的储位");
        }
        // 启用储位
        storageLocation.setStatus(WarehouseDictEnum.STATUS_ACTIVE.getCode());
        updateById(storageLocation);
    }

    @Override
    public void disable(String id) {
        // 查询仓库
        WmsStorageLocations storageLocation = this.getById(id);
        // 仓库不存在
        if(storageLocation==null) {
            throw new JeecgBootException("储位不存在");
        }
        String status = storageLocation.getStatus();
        // 储位状态为启用时方可禁用
        if(!status.equals(WarehouseDictEnum.STATUS_ACTIVE.getCode())) {
            throw new JeecgBootException("储位状态错误，只能禁用启用状态的储位");
        }
        // 禁用储位
        storageLocation.setStatus(WarehouseDictEnum.STATUS_INACTIVE.getCode());
        updateById(storageLocation);
    }

    /**
     * 生成储位编码
     * @param location
     * @return
     */
    public String generateLocationCode(WmsStorageLocations location) {
        if (location == null) {
            return "";
        }

        StringBuilder codeBuilder = new StringBuilder();

        String warehouseCode = getWarehouseCode(location.getWarehouseId());
        String zoneCode = getZoneName(location.getZoneId());
        String locationLine = location.getLocationLine();
        String locationRank = location.getLocationRank();
        String locationLayer = location.getLocationLayer();
        String locationCategory = location.getLocationCategory();

        if (StringUtils.isBlank(warehouseCode)) {
            return "";
        }

        codeBuilder.append(warehouseCode);

        if (StringUtils.isNotBlank(zoneCode)) {
            codeBuilder.append(zoneCode);
        }

        String lineStr = StringUtils.defaultIfBlank(locationLine, "0");
        try {
            int lineNum = Integer.parseInt(lineStr);
            codeBuilder.append("-").append(String.format("%02d", lineNum));
        } catch (NumberFormatException e) {
            codeBuilder.append("-").append(StringUtils.leftPad(lineStr, 2, '0'));
        }

        String rankStr = StringUtils.defaultIfBlank(locationRank, "0");
        try {
            int rankNum = Integer.parseInt(rankStr);
            codeBuilder.append("-").append(String.format("%02d", rankNum));
        } catch (NumberFormatException e) {
            codeBuilder.append("-").append(StringUtils.leftPad(rankStr, 2, '0'));
        }

        boolean isShelfType = "LIGHT_SHELF".equals(locationCategory) 
                || "HEAVY_SHELF".equals(locationCategory);
        
        if (isShelfType && StringUtils.isNotBlank(locationLayer)) {
            codeBuilder.append("-").append(locationLayer);
        }

        return codeBuilder.toString();
    }

    /**
     * 查询仓库编码
     * @param warehouseId
     * @return
     */
    public String getWarehouseCode(String warehouseId) {
        WmsWarehouses warehouse = wmsWarehousesMapper.selectById(warehouseId);
        return warehouse.getWarehouseCode();
    }
    /**
     * 查询储区名称
     * @param zoneId
     * @return
     */
    public String getZoneName(String zoneId) {
        WmsStorageZones zone = wmsStorageZonesMapper.selectById(zoneId);
        return zone.getZoneName();
    }
}