package com.neefull.fsp.web.monitor.controller;

import com.neefull.fsp.web.common.entity.FebsConstant;
import com.neefull.fsp.web.common.exception.RedisConnectException;
import com.neefull.fsp.web.common.utils.FebsUtil;
import com.neefull.fsp.web.monitor.endpoint.FebsMetricsEndpoint;
import com.neefull.fsp.web.monitor.entity.JvmInfo;
import com.neefull.fsp.web.monitor.entity.RedisInfo;
import com.neefull.fsp.web.monitor.entity.ServerInfo;
import com.neefull.fsp.web.monitor.entity.TomcatInfo;
import com.neefull.fsp.web.monitor.helper.FebsActuatorHelper;
import com.neefull.fsp.web.monitor.service.IRedisService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author pei.wang
 */
@Controller("monitorView")
@RequestMapping(FebsConstant.VIEW_PREFIX + "monitor")
public class ViewController {

    @Autowired
    private FebsActuatorHelper actuatorHelper;
    @Autowired
    private IRedisService redisService;

    @GetMapping("online")
    @RequiresPermissions("qff:online:view")
    public String online() {
        return FebsUtil.view("monitor/online");
    }

    @GetMapping("log")
    @RequiresPermissions("qff:log:view")
    public String log() {
        return FebsUtil.view("monitor/log");
    }

    @GetMapping("loginlog")
    @RequiresPermissions("qff:loginlog:view")
    public String loginLog() {
        return FebsUtil.view("monitor/loginLog");
    }

    @GetMapping("redis/info")
    @RequiresPermissions("qff:redis:view")
    public String getRedisInfo(Model model) throws RedisConnectException {
        List<RedisInfo> infoList = this.redisService.getRedisInfo();
        model.addAttribute("infoList", infoList);
        return FebsUtil.view("monitor/redisInfo");
    }

    @GetMapping("redis/terminal")
    @RequiresPermissions("qff:redis:terminal:view")
    public String redisTerminal(Model model) {
        String osName = System.getProperty("os.name");
        model.addAttribute("osName", osName);
        return FebsUtil.view("monitor/redisTerminal");
    }

    @GetMapping("httptrace")
    @RequiresPermissions("qff:httptrace:view")
    public String httptrace() {
        return FebsUtil.view("monitor/httpTrace");
    }

    @GetMapping("jvm")
    @RequiresPermissions("qff:jvm:view")
    public String jvmInfo(Model model) {
        List<FebsMetricsEndpoint.FebsMetricResponse> jvm = actuatorHelper.getMetricResponseByType("jvm");
        JvmInfo jvmInfo = actuatorHelper.getJvmInfoFromMetricData(jvm);
        model.addAttribute("jvm", jvmInfo);
        return FebsUtil.view("monitor/jvmInfo");
    }

    @GetMapping("tomcat")
    @RequiresPermissions("qff:tomcat:view")
    public String tomcatInfo(Model model) {
        List<FebsMetricsEndpoint.FebsMetricResponse> tomcat = actuatorHelper.getMetricResponseByType("tomcat");
        TomcatInfo tomcatInfo = actuatorHelper.getTomcatInfoFromMetricData(tomcat);
        model.addAttribute("tomcat", tomcatInfo);
        return FebsUtil.view("monitor/tomcatInfo");
    }

    @GetMapping("server")
    @RequiresPermissions("qff:server:view")
    public String serverInfo(Model model) {
        List<FebsMetricsEndpoint.FebsMetricResponse> jdbcInfo = actuatorHelper.getMetricResponseByType("jdbc");
        List<FebsMetricsEndpoint.FebsMetricResponse> systemInfo = actuatorHelper.getMetricResponseByType("system");
        List<FebsMetricsEndpoint.FebsMetricResponse> processInfo = actuatorHelper.getMetricResponseByType("process");

        ServerInfo serverInfo = actuatorHelper.getServerInfoFromMetricData(jdbcInfo, systemInfo, processInfo);
        model.addAttribute("server", serverInfo);
        return FebsUtil.view("monitor/serverInfo");
    }

}
