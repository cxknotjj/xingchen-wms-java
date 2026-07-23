package org.jeecg.modules.wms.goods.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.wms.goods.entity.WmsProductBrand;
import org.jeecg.modules.wms.goods.mapper.WmsProductBrandMapper;
import org.jeecg.modules.wms.goods.service.IWmsProductBrandService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 商品品牌
 * @Author: jeecg-boot
 * @Date:   2026-07-23
 * @Version: V1.0
 */
@Service
public class WmsProductBrandServiceImpl extends ServiceImpl<WmsProductBrandMapper, WmsProductBrand> implements IWmsProductBrandService {

    @Value("${jeecg.file-view-domain}")
    private String fileOninePreviewUrl;

    @Override
    public void add(WmsProductBrand wmsProductBrand) {
        // 创建一个新的wmsProductBrand
        WmsProductBrand wmsProductBrand1 = new WmsProductBrand();
        // 查询商品id是否存在
        if (baseMapper.selectById(wmsProductBrand.getId()) != null) {
            throw new JeecgBootException("商品id存在");
        }
        // 截取logo属性的url中的/temp/widget-timg_1755590887001.jpeg
        String logoUrl = wmsProductBrand.getLogo().replace(fileOninePreviewUrl,"");
        wmsProductBrand.setLogo(logoUrl);
        // 保存到数据库
        baseMapper.insert(wmsProductBrand);
    }
}
