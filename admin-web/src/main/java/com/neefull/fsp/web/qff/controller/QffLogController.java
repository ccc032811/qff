package com.neefull.fsp.web.qff.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.common.entity.FebsResponse;
import com.neefull.fsp.web.common.exception.FebsException;
import com.neefull.fsp.web.qff.entity.QffLog;
import com.neefull.fsp.web.qff.service.IQffLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Author: chengchengchu
 * @Date: 2020/1/15  11:06
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/qffLog")
public class QffLogController extends BaseController {

    @Autowired
    private IQffLogService qffLogService;


    /**查询日志
     * @param qffLog
     * @return
     */
    @GetMapping("/list")
    @RequiresPermissions("qffLog:view")
    public FebsResponse queryLogs(QffLog qffLog) throws FebsException {
        try {
            IPage<QffLog> qffLogPage = qffLogService.queryLogs(qffLog);
            Map<String, Object> dataTable = getDataTable(qffLogPage);
            return new FebsResponse().success().data(dataTable);
        } catch (Exception e) {
            String message = "查询日志失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }



}
