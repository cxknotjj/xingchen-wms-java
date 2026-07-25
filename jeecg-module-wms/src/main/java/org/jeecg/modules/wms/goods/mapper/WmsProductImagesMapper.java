package org.jeecg.modules.wms.goods.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.wms.goods.entity.WmsProductImages;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 商品图片表
 * @Author: jeecg-boot
 * @Date:   2026-07-23
 * @Version: V1.0
 */
public interface WmsProductImagesMapper extends BaseMapper<WmsProductImages> {

    /**
     * 查询商品图片列表
     * @param productId
     * @return
     */
    List<WmsProductImages> queryList(String productId);
}