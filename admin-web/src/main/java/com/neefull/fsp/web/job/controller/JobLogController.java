package com.neefull.fsp.web.job.controller;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.common.entity.FebsResponse;
import com.neefull.fsp.web.common.entity.QueryRequest;
import com.neefull.fsp.web.common.exception.FebsException;
import com.neefull.fsp.web.job.entity.JobLog;
import com.neefull.fsp.web.job.service.IJobLogService;
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
public class JobLogController extends BaseController {

    @Autowired
    private IJobLogService jobLogService;

    @GetMapping
    @RequiresPermissions("qf:job:log:view")
    public FebsResponse jobLogList(QueryRequest request, JobLog log) {
        Map<String, Object> dataTable = getDataTable(this.jobLogService.findJobLogs(request, log));
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("delete/{jobIds}")
    @RequiresPermissions("qf:job:log:delete")
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
    @RequiresPermissions("qf:job:log:export")
    public void export(QueryRequest request, JobLog jobLog, HttpServletResponse response) throws FebsException {
        try {
            List<JobLog> jobLogs = this.jobLogService.findJobLogs(request, jobLog).getRecords();
            ExcelKit.$Export(JobLog.class, response).downXlsx(jobLogs, false);
        } catch (Exception e) {
            String message = "导出Excel失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }
}
