package org.jeecg.modules.wms.analysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.wms.analysis.service.BigscreenService;
import org.jeecg.modules.wms.analysis.vo.TodoTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author Mr.M
 * @version 1.0
 * @description 首页大屏接口
 * @date 2025/9/17 14:51
 */
@Tag(name="首页大屏接口")
@RestController
@RequestMapping("/bigscreen")
public class BigscreenController {

    @Autowired
    private BigscreenService bigscreenService;

    /**
     * 查询待处理任务数
     */
    @Operation(summary="待办任务列表")
    @RequestMapping("/todo-tasks")
    public Result<List<TodoTask>> todoTaskList(HttpServletRequest  request) {
        String warehouseId = request.getHeader("x-warehouse-id");
        List<TodoTask> todoTaskList = bigscreenService.findTodoTaskList(warehouseId);
        return Result.OK(todoTaskList);
    }

    /**
     * 查询入库数量近12个月（自动补齐0月份）
     */
    @Operation(summary="入库数量近12个月")
    @RequestMapping("/inbound-numbers")
    public Result<List<Map<String, Object>>> inboundNumbers(HttpServletRequest  request) {
        String warehouseId = request.getHeader("x-warehouse-id");
        List<Map<String, Object>> inboundNumbers = bigscreenService.findInboundNumbers(warehouseId);
        return Result.OK(inboundNumbers);
    }

    /**
     * 查询出库数量近12个月（自动补齐0月份）
     */
    @Operation(summary="出库数量近12个月")
    @RequestMapping("/outbound-numbers")
    public Result<List<Map<String, Object>>> outboundNumbers(HttpServletRequest  request) {
        String warehouseId = request.getHeader("x-warehouse-id");
        List<Map<String, Object>> outboundNumbers = bigscreenService.findOutboundNumbers(warehouseId);
        return Result.OK(outboundNumbers);
    }

    /**
     * 查询出库数量Top
     */
    @Operation(summary="出库数量Top")
    @RequestMapping("/outbound-cargo-top")
    public Result<List<Map<String, Object>>> outboundCargoTop(HttpServletRequest  request) {
        String warehouseId = request.getHeader("x-warehouse-id");
        List<Map<String, Object>> outboundCargoTop = bigscreenService.findOutboundTop(warehouseId);
        return Result.OK(outboundCargoTop);
    }
}