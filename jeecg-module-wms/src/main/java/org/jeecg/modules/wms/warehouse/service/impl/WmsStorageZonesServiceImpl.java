package org.jeecg.modules.wms.warehouse.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.jeecg.modules.wms.warehouse.entity.WmsStorageZones;
import org.jeecg.modules.wms.warehouse.mapper.WmsStorageZonesMapper;
import org.jeecg.modules.wms.warehouse.service.IWmsStorageZonesService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 储区表
 * @Author: jeecg-boot
 * @Date:   2025-09-03
 * @Version: V1.0
 */
@Service
public class WmsStorageZonesServiceImpl extends ServiceImpl<WmsStorageZonesMapper, WmsStorageZones> implements IWmsStorageZonesService {

    @Override
    public IPage<WmsStorageZones> queryList(WmsStorageZones wmsStorageZones, Integer pageNo, Integer pageSize) {
        //将分页参数设置ThreadLocal中
        Page<WmsStorageZones> page = PageHelper.startPage(pageNo, pageSize);
        //调用mapper查询
        List<WmsStorageZones> wmsStorageZones1 = baseMapper.queryList(wmsStorageZones);
        PageDTO<WmsStorageZones> wmsStorageZonesPageDTO = new PageDTO<>();
        wmsStorageZonesPageDTO.setRecords(wmsStorageZones1);// 当前页的记录数
        wmsStorageZonesPageDTO.setTotal(page.getTotal());//总记录数
        wmsStorageZonesPageDTO.setCurrent(pageNo);//当前页码
        wmsStorageZonesPageDTO.setSize(pageSize);//每页条数
        wmsStorageZonesPageDTO.setPages(page.getPages());//总页数

        return wmsStorageZonesPageDTO;
    }
}
