package org.jeecg.modules.wms.wave.strategy;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrders;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrdersAllocation;
import org.jeecg.modules.wms.wave.service.IWmsWaveMasterService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 一品N件波次策略 - SIFQ_N
 * 条件：订单中只有一个SKU，且总数量为N
 */
@RequiredArgsConstructor
@Data
public class SIFQWaveStrategy extends IWaveStrategy {

    private final IWmsWaveMasterService wmsWaveMasterService;

    // 波次中商品数量
    private int quantity;

    @Override
    public void process(List<WmsOutOrders> orders,
                                    Map<String, List<WmsOutOrdersAllocation>> allocationsMap) {
        List<WmsOutOrders> matchedOrders = orders.stream()
            .filter(order -> order.getTotalSku() == 1 && order.getTotalQuantity() == quantity)
            .collect(Collectors.toList());

        //指定包裹策略为STANDARD_STRATEGY,后续打包作准备
        String shipmentStrategy="SPLIT_BY_WEIGHT_STRATEGY";

        if (!matchedOrders.isEmpty()) {
            wmsWaveMasterService.addWave(matchedOrders, getStrategyType(),shipmentStrategy);
        }

        List<WmsOutOrders> collect = orders.stream()
                .filter(order -> !matchedOrders.contains(order))
                .collect(Collectors.toList());
        if(collect.size()>0 && next()!=null){
            next().process(collect, allocationsMap);
        }
    }

    @Override
    public String getStrategyType() {
        return "SIFQ_" + quantity;
    }

    @Override
    public int getPriority() {
        return 2;
    }

}
