package com.neefull.fsp.web.monitor.controller;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.common.entity.FebsResponse;
import com.neefull.fsp.web.common.entity.QueryRequest;
import com.neefull.fsp.web.common.exception.FebsException;
import com.neefull.fsp.web.monitor.entity.Log;
import com.neefull.fsp.web.monitor.entity.LoginLog;
import com.neefull.fsp.web.monitor.service.ILoginLogService;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
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
@RestController
@RequestMapping("loginLog")
public class LoginLogController extends BaseController {

    @Autowired
    private ILoginLogService loginLogService;

    @GetMapping("list")
    @RequiresPermissions("qff:loginlog:view")
    public FebsResponse loginLogList(LoginLog loginLog, QueryRequest request) {
        Map<String, Object> dataTable = getDataTable(this.loginLogService.findLoginLogs(loginLog, request));
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("delete/{ids}")
    @RequiresPermissions("qff:loginlog:delete")
    public FebsResponse deleteLogss(@NotBlank(message = "{required}") @PathVariable String ids) throws FebsException {
        try {
            String[] loginLogIds = ids.split(StringPool.COMMA);
            this.loginLogService.deleteLoginLogs(loginLogIds);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "删除日志失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }

    @GetMapping("excel")
    @RequiresPermissions("qff:loginlog:export")
    public void export(QueryRequest request, LoginLog loginLog, HttpServletResponse response) throws FebsException {
        try {
            List<LoginLog> loginLogs = this.loginLogService.findLoginLogs(loginLog, request).getRecords();
            ExcelKit.$Export(Log.class, response).downXlsx(loginLogs, false);
        } catch (Exception e) {
            String message = "导出Excel失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }
}
