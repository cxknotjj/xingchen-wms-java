package org.jeecg.modules.wms.wave.strategy;

import lombok.RequiredArgsConstructor;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrders;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrdersAllocation;
import org.jeecg.modules.wms.wave.service.IWmsWaveMasterService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 单品单件波次策略 - SISQ
 * 条件：订单中只有一个SKU，且总数量为1
 */
@Component
@RequiredArgsConstructor
public class SISQWaveStrategy extends IWaveStrategy {

    private final IWmsWaveMasterService wmsWaveMasterService;

    @Override
    public void process(List<WmsOutOrders> orders,
                                    Map<String, List<WmsOutOrdersAllocation>> allocationsMap) {
        List<WmsOutOrders> matchedOrders = orders.stream()
            .filter(order -> order.getTotalSku() == 1 && order.getTotalQuantity() == 1)
            .collect(Collectors.toList());

        //包裹策略为STANDARD_STRATEGY
        String shipmentStrategy="STANDARD_STRATEGY";

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
        return "SISQ";
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
