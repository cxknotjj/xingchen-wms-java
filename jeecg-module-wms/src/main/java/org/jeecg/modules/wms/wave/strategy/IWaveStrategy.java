package org.jeecg.modules.wms.wave.strategy;

import org.jeecg.modules.wms.outorder.entity.WmsOutOrders;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrdersAllocation;

import java.util.List;
import java.util.Map;

/**
 * 波次策略接口
 */
public abstract class IWaveStrategy {

    private IWaveStrategy nextStrategy;

    public void setNextStrategy(IWaveStrategy nextStrategy) {
        this.nextStrategy = nextStrategy;
    }

    /**
     * 处理波次策略
     * @param orders 待处理的订单列表
     * @param allocationsMap 订单分配明细映射
     * @return 处理后的订单列表(已分配波次的订单会被移除)
     */
    abstract void process(List<WmsOutOrders> orders,
                             Map<String, List<WmsOutOrdersAllocation>> allocationsMap);

    /**
     * 获取策略类型
     */
    abstract String getStrategyType();

    /**
     * 获取策略优先级(数值越小优先级越高)
     */
    abstract int getPriority();

    /**
     * 下一个策略
     */
    public IWaveStrategy  next(){
        return nextStrategy;
    }
}
