package org.jeecg.modules.wms.analysis.service.impl;

import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.wms.analysis.mapper.BigscreenMapper;
import org.jeecg.modules.wms.analysis.service.BigscreenService;
import org.jeecg.modules.wms.analysis.vo.TodoTask;
import org.jeecg.modules.wms.config.WarehouseDictEnum;
import org.jeecg.modules.wms.warehouse.service.IWmsWarehousesService;
import org.jeecg.modules.wms.wmstask.entity.WmsTasks;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BigscreenServiceImpl implements BigscreenService {
    @Autowired
    private BigscreenMapper bigscreenMapper;

    @Autowired
    private IWmsTasksService wmsTasksService;

    @Autowired
    private IWmsWarehousesService wmsWarehousesService;

    @Override
    @Cacheable(value = "sys:cache:bigscreen:todotasklist",key = "#warehouseId",cacheManager = "cacheManager30Minutes")
    public List<TodoTask> findTodoTaskList(String warehouseId) {
        //所有待收货任务总量
        int totalReceivingTaskCount = 0;
        //所有待上架任务总量
        int totalPutawayTaskCount = 0;
        //所有待拣货任务总量
        int totalPickingTaskCount = 0;
        //已完成收货任务数量
        int completedReceivingTaskCount = 0;
        //已完成上架任务数量
        int completedPutawayTaskCount = 0;
        //已完成拣货任务数量
        int completedPickingTaskCount = 0;
        // 创建一个tasks对象
        WmsTasks tasks = new WmsTasks();
        tasks.setTargetWarehouseId(warehouseId);
        // 当前时间，需要格式化位yyyy-MM-dd
        String currentDate = DateUtils.formatDate(new Date(), "yyyy-MM-dd");
        tasks.setCreateTimeString("2026-08-01");
        // 先统计当前的总任务数量
        List<WmsTasks> allTodoTaskList = bigscreenMapper.countTaskList(tasks);
        // 遍历alltodoTaskList，向totalReceivingTaskCount、totalPutawayTaskCount、totalPickingTaskCount赋值
        for (WmsTasks todoTask : allTodoTaskList) {
            if (todoTask.getTaskType().equals(WarehouseDictEnum.TASK_TYPE_RECEIVING.getCode())) {
                totalReceivingTaskCount = todoTask.getTaskCount();
            } else if (todoTask.getTaskType().equals(WarehouseDictEnum.TASK_TYPE_PUTAWAY.getCode())) {
                totalPutawayTaskCount = todoTask.getTaskCount();
            } else if (todoTask.getTaskType().equals(WarehouseDictEnum.TASK_TYPE_PICKING.getCode())) {
                totalPickingTaskCount = todoTask.getTaskCount();
            }
        }
        // 再统计已完成的任务数量
        // 在原有任务基础上添加任务状态为已完成的任务
        tasks.setTaskStatus(WarehouseDictEnum.TASK_STATUS_COMPLETED.getCode());
        List<WmsTasks> completedTaskList = bigscreenMapper.countTaskList(tasks);
        for (WmsTasks todoTask : completedTaskList) {
            if (todoTask.getTaskType().equals(WarehouseDictEnum.TASK_TYPE_RECEIVING.getCode())) {
                completedReceivingTaskCount = todoTask.getTaskCount();
            } else if (todoTask.getTaskType().equals(WarehouseDictEnum.TASK_TYPE_PUTAWAY.getCode())) {
                completedPutawayTaskCount = todoTask.getTaskCount();
            } else if (todoTask.getTaskType().equals(WarehouseDictEnum.TASK_TYPE_PICKING.getCode())) {
                completedPickingTaskCount = todoTask.getTaskCount();
            }
        }
        // 计算出代办任务的数量
        int todoReceivingTaskCount = totalReceivingTaskCount - completedReceivingTaskCount;
        int todoPutawayTaskCount = totalPutawayTaskCount - completedPutawayTaskCount;
        int todoPickingTaskCount = totalPickingTaskCount - completedPickingTaskCount;
        // 创建一个list，用于存储待办任务
        List<TodoTask> todoTaskList = Arrays.asList(
                new TodoTask("待收货任务", "icon-receiving", todoReceivingTaskCount, totalReceivingTaskCount, currentDate),
                new TodoTask("待上架任务", "icon-putaway", todoPutawayTaskCount, totalPutawayTaskCount, currentDate),
                new TodoTask("待拣货任务", "icon-picking", todoPickingTaskCount, totalPickingTaskCount, currentDate),
                new TodoTask("待发货任务", "icon-complete", 0, 0, currentDate)
        );
        return todoTaskList;
    }

    @Override
//    @Cacheable(value = "sys:cache:bigscreen:inboundNumbers",key = "#warehouseId",cacheManager = "cacheManager30Minutes")
    public List<Map<String, Object>> findInboundNumbers(String warehouseId) {
        List<Map<String, Object>> dbList = bigscreenMapper.findInboundNumbers(warehouseId);
        // 1. 先补齐12个月（缺的月份 value=0）
        List<Map<String, Object>> fullList = cancelZeroMonths(dbList);
        // 2. 再取消月份的补0（09→9）
        return fillZeroMonths(fullList);
    }

    @Override
//    @Cacheable(value = "sys:cache:bigscreen:outboundNumbers",key = "#warehouseId",cacheManager = "cacheManager30Minutes")
    public List<Map<String, Object>> findOutboundNumbers(String warehouseId) {
        List<Map<String, Object>> dbList = bigscreenMapper.findOutboundNumbers(warehouseId);
        // 1. 先补齐12个月（缺的月份 value=0）
        List<Map<String, Object>> fullList = cancelZeroMonths(dbList);
        // 2. 再取消月份的补0（09→9）
        return fillZeroMonths(fullList);
    }

    /**
     * 通用工具方法：补齐近12个月，没数据的月份补0
     */
    private List<Map<String, Object>> fillZeroMonths(List<Map<String, Object>> dbList) {
        for (int i = 1; i <= 12; i++) {
            Boolean isExist = false;
            for (Map<String, Object> map : dbList) {
                String month = map.get("name").toString();
                if (month.equals(String.valueOf(i))) {
                    isExist = true;
                    map.put("name", i + "月");
                }
            }
            if (!isExist) {
                dbList.add(Map.of("name", i + "月", "value", 0));
            }
        }
        return dbList;
    }

    /**
     * 通用工具方法：取消月份的补0操作
     * 例："09" → "9"，"10" → "10"，"12" → "12"
     * 同时支持 name 是 "2025-09" 这种年月格式，会把 "-" 后面的月份去0，输出 "2025-9"
     */
    private List<Map<String, Object>> cancelZeroMonths(List<Map<String, Object>> dbList) {
        if (dbList == null || dbList.isEmpty()) {
            return dbList;
        }
        // 遍历每一条数据，修改 name 字段里的月份去前导0
        for (Map<String, Object> row : dbList) {
            Object nameObj = row.get("name");
            if (nameObj == null) {
                continue;
            }
            String name = nameObj.toString();

            // ===== 情况1：name 是 "2025-09" 这种带横杠的年月 =====
            if (name.contains("-")) {
                int idx = name.lastIndexOf('-');
                String yearPart  = name.substring(0, idx);
                String monthPart = name.substring(idx + 1);
                try {
                    row.put("name", yearPart + "-" + Integer.parseInt(monthPart));
                } catch (NumberFormatException ignored) {
                    // 月份不是数字就原样返回
                }
            }
            // ===== 情况2：name 就是纯月份 "09" =====
            else {
                try {
                    row.put("name", String.valueOf(Integer.parseInt(name)));
                } catch (NumberFormatException ignored) {
                    // 不是纯数字就原样返回
                }
            }
        }
        return dbList;
    }
}