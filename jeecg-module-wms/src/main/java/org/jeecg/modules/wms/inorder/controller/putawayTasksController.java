package org.jeecg.modules.wms.inorder.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.wms.wmstask.entity.WmsTasks;
import org.jeecg.modules.wms.wmstask.entity.WmsTasksRecords;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksRecordsService;
import org.jeecg.modules.wms.wmstask.service.IWmsTasksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name="任务表")
@RequestMapping("/inorder/putawayTasks")
@Slf4j
public class putawayTasksController {
    @Autowired
    private IWmsTasksService wmsTasksService;
    @Autowired
    private IWmsTasksRecordsService wmsTasksRecordsService;

    /**
     * 待收货任务查询（分页）
     */
    @Operation(summary = "待收货任务查询")
    @GetMapping(value = "/list") // 修正：原 OCR 为 "/1ist"（数字1误识）
    public Result<IPage<WmsTasks>> queryPageList(WmsTasks wmsTasks,
                                                 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                 HttpServletRequest req) {
//        // [补全] 原代码此行残缺，仅余 "req.getParameterMap());"
//        QueryWrapper<WmsTasks> queryWrapper = QueryGenerator.initQueryWrapper(wmsTasks, req.getParameterMap());
//        Page<WmsTasks> page = new Page<>(pageNo, pageSize);
//        IPage<WmsTasks> pageList = wmsTasksService.page(page, queryWrapper);
        // 当前用户id
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        wmsTasks.setOperator(sysUser.getId());
        IPage<WmsTasks> pageList = wmsTasksService.list(wmsTasks, pageNo, pageSize);
        return Result.OK(pageList);
    }

    /**
     * 收货记录查询（分页）
     */
    @Operation(summary = "收货记录查询")
    @GetMapping(value = "/records")
    public Result<IPage<WmsTasksRecords>> records(WmsTasksRecords wmsTasksRecords,
                                                  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                  HttpServletRequest req) {
        QueryWrapper<WmsTasksRecords> queryWrapper = QueryGenerator.initQueryWrapper(wmsTasksRecords, req.getParameterMap());
        Page<WmsTasksRecords> page = new Page<>(pageNo, pageSize);
        IPage<WmsTasksRecords> pageList = wmsTasksRecordsService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 上架
     */
    @AutoLog(value = "上架")
    @Operation(summary = "上架")
    @PostMapping(value = "/addRecords")
    public Result<String> addRecords(@RequestBody WmsTasksRecords wmsTasksRecords) {
        // taskId
        String taskId = wmsTasksRecords.getId();
        wmsTasksRecords.setTaskId(taskId);
        wmsTasksRecords.setId(null);
        wmsTasksService.shelf(wmsTasksRecords);
        return Result.OK("上架成功!");
    }
}
