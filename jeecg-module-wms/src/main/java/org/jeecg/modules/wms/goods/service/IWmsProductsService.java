package org.jeecg.modules.wms.goods.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.wms.goods.entity.WmsProducts;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 商品信息表
 * @Author: jeecg-boot
 * @Date:   2026-07-23
 * @Version: V1.0
 */
public interface IWmsProductsService extends IService<WmsProducts> {

    /**
     * 分页列表查询
     * @param wmsProducts
     * @param pageNo
     * @param pageSize
     * @return
     */
    IPage<WmsProducts> queryList(WmsProducts wmsProducts, Integer pageNo, Integer pageSize);
}
