package org.jeecg.modules.wms.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
 * @Date:   2026-07-20
 * @Version: V1.0
 */
@Service
public class WmsCargoOwnersServiceImpl extends ServiceImpl<WmsCargoOwnersMapper, WmsCargoOwners> implements IWmsCargoOwnersService {

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 新增货主
     * @param wmsCargoOwners
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(WmsCargoOwners wmsCargoOwners) {
        // 生成货主ID
        String code = generateCargoOwnerId();
        wmsCargoOwners.setOwnerCode(code);
        // 保存货主信息
        save(wmsCargoOwners);
    }

    /**
     * 生成货主编号
     */

    public String generateCargoOwnerId() {
        // key
        String key = "WMS_CARGO_OWNERS_CODE";

        long incr = 0;
        try {
            incr = redisUtil.incr(key, 1);
        } catch (Exception e) {
            throw new JeecgBootException("生成货主编号失败");
        }

        // C+ 5位序号
        String code = "C" + String.format("%05d", incr);

        // 检查数据库中是否已存在该编号，解决Redis计数器重置导致重复的问题
        while (isCodeExists(code)) {
            incr = redisUtil.incr(key, 1);
            code = "C" + String.format("%05d", incr);
        }
        return code;
    }

    /**
     * 检查货主编号是否已存在
     * @param code 货主编号
     * @return true-已存在，false-不存在
     */
    private boolean isCodeExists(String code) {
        QueryWrapper<WmsCargoOwners> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("owner_code", code);
        return count(queryWrapper) > 0;
    }
}
