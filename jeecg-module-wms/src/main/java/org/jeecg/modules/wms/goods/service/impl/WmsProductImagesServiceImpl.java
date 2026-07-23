package org.jeecg.modules.wms.goods.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.wms.goods.entity.WmsProductBrand;
import org.jeecg.modules.wms.goods.entity.WmsProductImages;
import org.jeecg.modules.wms.goods.mapper.WmsProductImagesMapper;
import org.jeecg.modules.wms.goods.service.IWmsProductImagesService;
import org.jeecg.modules.wms.warehouse.entity.WmsStorageLocations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 商品图片表
 * @Author: jeecg-boot
 * @Date:   2026-07-23
 * @Version: V1.0
 */
@Service
public class WmsProductImagesServiceImpl extends ServiceImpl<WmsProductImagesMapper, WmsProductImages> implements IWmsProductImagesService {
    @Value("${jeecg.file-view-domain}")
    private String fileOninePreviewUrl;

    @Override
    public IPage<WmsProductImages> queryList(String productId, Integer pageNo, Integer pageSize) {
        // 将分页参数设置到Threadlocal中
        Page<WmsProductImages> page = PageHelper.startPage(pageNo, pageSize);
        // 调用mapper查询
        List<WmsProductImages> wmsProductImages1 = baseMapper.queryList(productId);
        PageDTO<WmsProductImages> pageDTO = new PageDTO<>();
        // 当前页记录数
        pageDTO.setRecords(wmsProductImages1);
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
    public void add(WmsProductImages wmsProductImages) {
        // 创建一个新的wmsProductImages
        WmsProductImages wmsProductImages1 = new WmsProductImages();
        // 查询图片id是否存在
        if (baseMapper.selectById(wmsProductImages.getId()) != null) {
            throw new JeecgBootException("图片id存在");
        }
        // 截取original属性的url中的/temp/widget-timg_1755590887001.jpeg
        String imageUrl = wmsProductImages.getOriginal().replace(fileOninePreviewUrl,"");
        wmsProductImages.setOriginal(imageUrl);
        // 保存到数据库
        baseMapper.insert(wmsProductImages);
    }
}
