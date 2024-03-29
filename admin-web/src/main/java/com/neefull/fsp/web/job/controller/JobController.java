package com.neefull.fsp.web.job.controller;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.neefull.fsp.web.common.annotation.Log;
import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.common.entity.FebsResponse;
import com.neefull.fsp.web.common.entity.QueryRequest;
import com.neefull.fsp.web.common.exception.FebsException;
import com.neefull.fsp.web.common.utils.DateUtil;
import com.neefull.fsp.web.job.entity.Job;
import com.neefull.fsp.web.job.service.IJobService;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * @author pei.wang
 */
@Slf4j
@Validated
@RestController
@RequestMapping("job")
public class JobController extends BaseController {

    @Autowired
    private IJobService jobService;

    @GetMapping
    @RequiresPermissions("qf:job:view")
    public FebsResponse jobList(QueryRequest request, Job job) {
        Map<String, Object> dataTable = getDataTable(this.jobService.findJobs(request, job));
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("cron/check")
    public boolean checkCron(String cron) {
        try {
            return CronExpression.isValidExpression(cron);
        } catch (Exception e) {
            return false;
        }
    }

    @Log("新增定时任务")
    @PostMapping
    @RequiresPermissions("qf:job:add")
    public FebsResponse addJob(@Valid Job job) throws FebsException {
        try {
            this.jobService.createJob(job);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "新增定时任务失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }

    @Log("删除定时任务")
    @GetMapping("delete/{jobIds}")
    @RequiresPermissions("qf:job:delete")
    public FebsResponse deleteJob(@NotBlank(message = "{required}") @PathVariable String jobIds) throws FebsException {
        try {
            String[] ids = jobIds.split(StringPool.COMMA);
            this.jobService.deleteJobs(ids);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "删除定时任务失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }

    @Log("修改定时任务")
    @PostMapping("update")
    public FebsResponse updateJob(@Valid Job job) throws FebsException {

        Job oldJob = jobService.findJob(job.getJobId());
        if(job.getBeanName().equals("acquireOneSoap")){
            String toDate = job.getToDate();
            String fromTime = job.getFromTime();
            String toTime = job.getToTime();
            String message = job.getParams();
            if(StringUtils.isNotEmpty(toDate)&&StringUtils.isNotEmpty(fromTime)&&StringUtils.isNotEmpty(toTime)){
                message = toDate+","+fromTime+","+toTime+","+message;
                if(StringUtils.isNotEmpty(message)){
                    job.setParams(message);
                }
            }else {
                job.setParams(oldJob.getParams());
            }
        }

        try {
            this.jobService.updateJob(job);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "修改定时任务失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }



    @Log("执行定时任务")
    @RequiresPermissions("qf:job:run")
    @GetMapping("run/{jobIds}")
    public FebsResponse runJob(@NotBlank(message = "{required}") @PathVariable String jobIds) throws FebsException {
        try {
            this.jobService.run(jobIds);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "执行定时任务失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }

    @Log("暂停定时任务")
    @GetMapping("pause/{jobIds}")
    @RequiresPermissions("qf:job:pause")
    public FebsResponse pauseJob(@NotBlank(message = "{required}") @PathVariable String jobIds) throws FebsException {
        try {
            this.jobService.pause(jobIds);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "暂停定时任务失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }

    @Log("恢复定时任务")
    @GetMapping("resume/{jobIds}")
    @RequiresPermissions("qf:job:resume")
    public FebsResponse resumeJob(@NotBlank(message = "{required}") @PathVariable String jobIds) throws FebsException {
        try {
            this.jobService.resume(jobIds);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "恢复定时任务失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }

    @GetMapping("excel")
    @RequiresPermissions("qf:job:export")
    public void export(QueryRequest request, Job job, HttpServletResponse response) throws FebsException {
        try {
            List<Job> jobs = this.jobService.findJobs(request, job).getRecords();
            ExcelKit.$Export(Job.class, response).downXlsx(jobs, false);
        } catch (Exception e) {
            String message = "导出Excel失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }
}
