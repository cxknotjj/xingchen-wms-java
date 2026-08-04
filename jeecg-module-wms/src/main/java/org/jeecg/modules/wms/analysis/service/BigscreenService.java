package org.jeecg.modules.wms.analysis.service;

import org.jeecg.modules.wms.analysis.vo.TodoTask;

import java.util.List;
import java.util.Map;

public interface BigscreenService {
    List<TodoTask> findTodoTaskList(String warehouseId);

    /**
     * 查询入库数量近12个月（自动补齐0月份）
     * @param warehouseId
     * @return
     */
    List<Map<String, Object>> findInboundNumbers(String warehouseId);

    /**
     * 查询出库数量近12个月（自动补齐0月份）
     * @param warehouseId
     * @return
     */
    List<Map<String, Object>> findOutboundNumbers(String warehouseId);
}