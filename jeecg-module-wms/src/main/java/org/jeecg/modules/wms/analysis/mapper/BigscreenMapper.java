package org.jeecg.modules.wms.analysis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.wms.analysis.vo.TodoTask;
import org.jeecg.modules.wms.wmstask.entity.WmsTasks;

import java.util.List;
import java.util.Map;

/**
 * 大屏数据接口
 */
@Mapper
public interface BigscreenMapper {
    /**
     * 统计待办任务
     */
    List<WmsTasks> countTaskList(WmsTasks wmsTasks);

    /***
     * 查询入库数量近12个月
     * @param warehouseId
     * @return
     */
    List<Map<String, Object>> findInboundNumbers(@Param("warehouseId") String warehouseId);

    /***
     * 查询出库数量近12个月
     * @param warehouseId
     * @return
     */
    List<Map<String, Object>> findOutboundNumbers(@Param("warehouseId") String warehouseId);

    /**
     * 查询出库数量Top
     */
    List<Map<String, Object>> findOutboundTop(@Param("warehouseId") String warehouseId);

}