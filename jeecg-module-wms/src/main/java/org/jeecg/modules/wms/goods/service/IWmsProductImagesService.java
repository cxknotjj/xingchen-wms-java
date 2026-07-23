package org.jeecg.modules.wms.goods.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.wms.goods.entity.WmsProductImages;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 商品图片表
 * @Author: jeecg-boot
 * @Date:   2026-07-23
 * @Version: V1.0
 */
public interface IWmsProductImagesService extends IService<WmsProductImages> {

    /**
     * 分页列表查询图片
     * @param productId
     * @param pageNo
     * @param pageSize
     * @return
     */
    IPage<WmsProductImages> queryList(String productId, Integer pageNo, Integer pageSize);

    /**
     * 添加商品图片
     * @param wmsProductImages
     */
    void add(WmsProductImages wmsProductImages);
}
