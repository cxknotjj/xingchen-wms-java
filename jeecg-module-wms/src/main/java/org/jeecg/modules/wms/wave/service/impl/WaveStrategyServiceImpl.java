package org.jeecg.modules.wms.wave.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.apache.poi.ss.formula.functions.T;
import org.jeecg.modules.wms.wave.entity.WmsWaveStrategy;
import org.jeecg.modules.wms.wave.mapper.WaveStrategyMapper;
import org.jeecg.modules.wms.wave.service.IWaveStrategyService;
import org.jeecg.modules.wms.wave.strategy.IWaveStrategy;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 波次策略表
 * @Author: jeecg-boot
 * @Date:   2025-06-03
 * @Version: V1.0
 */
@Service
public class WaveStrategyServiceImpl extends ServiceImpl<WaveStrategyMapper, WmsWaveStrategy> implements IWaveStrategyService {

    @Override
    public WmsWaveStrategy getStrategyByCode(String strategyType) {
        LambdaQueryWrapper<WmsWaveStrategy> query = new LambdaQueryWrapper<>();
            query.eq(WmsWaveStrategy::getStrategyCode, strategyType);
        return this.getOne(query);

    }
}
