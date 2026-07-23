package org.jeecg.modules.wms.goods.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.wms.goods.entity.WmsProducts;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 商品信息表
 * @Author: jeecg-boot
 * @Date:   2026-07-23
 * @Version: V1.0
 */
public interface WmsProductsMapper extends BaseMapper<WmsProducts> {

    /**
     * 分页查询商品列表
     */
    List<WmsProducts> queryList(@Param("wmsProducts") WmsProducts wmsProducts);
}
