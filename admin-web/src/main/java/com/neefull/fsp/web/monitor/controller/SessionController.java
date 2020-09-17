package com.neefull.fsp.web.monitor.controller;

import com.neefull.fsp.web.common.entity.FebsResponse;
import com.neefull.fsp.web.monitor.entity.ActiveUser;
import com.neefull.fsp.web.monitor.service.ISessionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pei.wang
 */
@RestController
@RequestMapping("session")
public class SessionController {

    @Autowired
    private ISessionService sessionService;

    @GetMapping("list")
    @RequiresPermissions("qff:online:view")
    public FebsResponse list(String username) {
        List<ActiveUser> list = sessionService.list(username);
        Map<String, Object> data = new HashMap<>();
        data.put("rows", list);
        data.put("total", CollectionUtils.size(list));
        return new FebsResponse().success().data(data);
    }

    @GetMapping("delete/{id}")
    @RequiresPermissions("qff:user:kickout")
    public FebsResponse forceLogout(@PathVariable String id) {
        sessionService.forceLogout(id);
        return new FebsResponse().success();
    }
}
