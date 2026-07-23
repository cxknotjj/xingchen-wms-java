package org.jeecg.modules.wms.goods.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.jeecg.modules.wms.goods.entity.WmsProducts;
import org.jeecg.modules.wms.goods.mapper.WmsProductsMapper;
import org.jeecg.modules.wms.goods.service.IWmsProductsService;
import org.jeecg.modules.wms.warehouse.entity.WmsStorageZones;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 商品信息表
 * @Author: jeecg-boot
 * @Date:   2026-07-23
 * @Version: V1.0
 */
@Service
public class WmsProductsServiceImpl extends ServiceImpl<WmsProductsMapper, WmsProducts> implements IWmsProductsService {

    @Override
    public IPage<WmsProducts> queryList(WmsProducts wmsProducts, Integer pageNo, Integer pageSize) {
        // 将分页参数设置到Threadlocal中
        Page<WmsProducts> page = PageHelper.startPage(pageNo, pageSize);
        // 调用mapper查询
        List<WmsProducts> wmsProducts1 = baseMapper.queryList(wmsProducts);
        PageDTO<WmsProducts> pageDTO = new PageDTO<>();
        // 当前页记录数
        pageDTO.setRecords(wmsProducts1);
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
