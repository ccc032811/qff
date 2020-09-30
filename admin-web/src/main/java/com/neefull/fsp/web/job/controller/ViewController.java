package com.neefull.fsp.web.job.controller;

import com.neefull.fsp.web.common.entity.FebsConstant;
import com.neefull.fsp.web.common.utils.FebsUtil;
import com.neefull.fsp.web.job.entity.Job;
import com.neefull.fsp.web.job.service.IJobService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.constraints.NotBlank;

/**
 * @author pei.wang
 */
@Controller("jobView")
@RequestMapping(FebsConstant.VIEW_PREFIX + "job")
public class ViewController {

    @Autowired
    private IJobService jobService;

    @GetMapping("job")
    @RequiresPermissions("qf:job:view")
    public String online() {
        return FebsUtil.view("job/job");
    }

    @GetMapping("job/add")
    @RequiresPermissions("qf:job:add")
    public String jobAdd() {
        return FebsUtil.view("job/jobAdd");
    }

    @GetMapping("job/update/{jobId}")
    @RequiresPermissions("qf:job:update")
    public String jobUpdate(@NotBlank(message = "{required}") @PathVariable Long jobId, Model model) {
        Job job = jobService.findJob(jobId);
        model.addAttribute("job", job);
        return FebsUtil.view("job/jobUpdate");
    }

    @GetMapping("job/updateProcess/{jobId}")
    @RequiresPermissions("qf:job:update")
    public String updateProcess(@NotBlank(message = "{required}") @PathVariable Long jobId, Model model) {
        Job job = jobService.findJob(jobId);
        model.addAttribute("job", job);
        return FebsUtil.view("job/update");
    }

    @GetMapping("log")
    @RequiresPermissions("qf:job:log:view")
    public String log() {
        return FebsUtil.view("job/jobLog");
    }

}
