package org.jeecg.modules.wms.wave.strategy;

import org.jeecg.modules.wms.outorder.entity.WmsOutOrders;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrdersAllocation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *  调用波次策略的客户端类,enter
 */


public class WaveCrateClient {

    /**
     * 策略链的头
     */
    private IWaveStrategy firstStrategy;

    /**
     *  构造方法
     *  所有的波次策略
     */
    public WaveCrateClient(List<IWaveStrategy> strategies,List<String> selectedStrategies) {
        // 筛选中页面选择的策略，按照优先级升序
        List<IWaveStrategy> collect = strategies.stream()
                .filter(strategy -> selectedStrategies.contains(strategy.getStrategyType()))
                .sorted((s1, s2) -> s1.getPriority() - s2.getPriority())
                .collect(Collectors.toList());
        // 链头
        this.firstStrategy = collect.get(0);
        // 将所使用的策略组成一个链
        for (int i = 0; i < collect.size() - 1; i++) {
            collect.get(i).setNextStrategy(collect.get(i + 1));
        }
    }

    public void process(List<WmsOutOrders> orders,
                        Map<String, List<WmsOutOrdersAllocation>> allocationsMap) {
        // 调用链头处理策略链
        this.firstStrategy.process(orders, allocationsMap);
    }
}
