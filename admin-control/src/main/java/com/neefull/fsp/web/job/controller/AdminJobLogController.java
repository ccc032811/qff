package com.neefull.fsp.web.job.controller;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.common.entity.FebsResponse;
import com.neefull.fsp.web.common.entity.QueryRequest;
import com.neefull.fsp.web.common.exception.FebsException;
import com.neefull.fsp.web.job.entity.AdminJobLog;
import com.neefull.fsp.web.job.service.IAdminJobLogService;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * @author pei.wang
 */
@Slf4j
@Validated
@RestController
@RequestMapping("jobLog")
public class AdminJobLogController extends BaseController {

    @Autowired
    private IAdminJobLogService jobLogService;

    @GetMapping
    @RequiresPermissions("job:log:view")
    public FebsResponse jobLogList(QueryRequest request, AdminJobLog log) {
        Map<String, Object> dataTable = getDataTable(this.jobLogService.findJobLogs(request, log));
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("delete/{jobIds}")
    @RequiresPermissions("job:log:delete")
    public FebsResponse deleteJobLog(@NotBlank(message = "{required}") @PathVariable String jobIds) throws FebsException {
        try {
            String[] ids = jobIds.split(StringPool.COMMA);
            this.jobLogService.deleteJobLogs(ids);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "删除调度日志失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }

    @GetMapping("excel")
    @RequiresPermissions("job:log:export")
    public void export(QueryRequest request, AdminJobLog jobLog, HttpServletResponse response) throws FebsException {
        try {
            List<AdminJobLog> jobLogs = this.jobLogService.findJobLogs(request, jobLog).getRecords();
            ExcelKit.$Export(AdminJobLog.class, response).downXlsx(jobLogs, false);
        } catch (Exception e) {
            String message = "导出Excel失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }
}
