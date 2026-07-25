package org.jeecg.modules.wms.goods.service.impl;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.wms.goods.entity.WmsCargoOwners;
import org.jeecg.modules.wms.goods.mapper.WmsCargoOwnersMapper;
import org.jeecg.modules.wms.goods.service.IWmsCargoOwnersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description: 货主表
 * @Author: jeecg-boot
 * @Date:   2025-09-02
 * @Version: V1.0
 */
@Service
public class WmsCargoOwnersServiceImpl extends ServiceImpl<WmsCargoOwnersMapper, WmsCargoOwners> implements IWmsCargoOwnersService {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(WmsCargoOwners wmsCargoOwners) {
        //生成货主编码 C+5位序号，序号使用redis自增序号实现
        String code = generateOwnerCode();
        wmsCargoOwners.setOwnerCode( code);
        //保存到数据库
        save(wmsCargoOwners);
    }
    /**
     * 生成货主编码
     */
    public String generateOwnerCode() {

        //key
        String key = "WMS_CARGO_OWNERS_CODE";

        long incr = 0;
        try {
            incr = redisUtil.incr(key, 1);
        } catch (Exception e) {
            throw new JeecgBootException("生成货主编码出错");
        }

        //5位序号
        String code = String.format("%05d", incr);

        return "C"+code;

    }

}
