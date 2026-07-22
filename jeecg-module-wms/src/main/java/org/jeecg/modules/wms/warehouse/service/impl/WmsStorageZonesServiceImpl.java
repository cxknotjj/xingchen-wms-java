package org.jeecg.modules.wms.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.jeecg.modules.wms.warehouse.entity.WmsStorageZones;
import org.jeecg.modules.wms.warehouse.mapper.WmsStorageZonesMapper;
import org.jeecg.modules.wms.warehouse.service.IWmsStorageZonesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 储区表
 * @Author: jeecg-boot
 * @Date:   2026-07-21
 * @Version: V1.0
 */
@Service
public class WmsStorageZonesServiceImpl extends ServiceImpl<WmsStorageZonesMapper, WmsStorageZones> implements IWmsStorageZonesService {


    @Override
    public IPage<WmsStorageZones> queryList(WmsStorageZones wmsStorageZones, Integer pageNo, Integer pageSize) {
        // 将分页参数设置到Threadlocal中
        Page<WmsStorageZones> page = PageHelper.startPage(pageNo, pageSize);
        // 调用mapper查询
        List<WmsStorageZones> wmsStorageZones1 = baseMapper.queryList(wmsStorageZones);
        PageDTO<WmsStorageZones> pageDTO = new PageDTO<>();
        // 当前页记录数
        pageDTO.setRecords(wmsStorageZones1);
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
}
