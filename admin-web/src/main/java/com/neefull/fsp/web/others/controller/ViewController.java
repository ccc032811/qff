package com.neefull.fsp.web.others.controller;

import com.neefull.fsp.web.common.entity.FebsConstant;
import com.neefull.fsp.web.common.utils.FebsUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author pei.wang
 */
@Controller("othersView")
@RequestMapping(FebsConstant.VIEW_PREFIX + "others")
public class ViewController {

    @GetMapping("febs/form")
    @RequiresPermissions("qff:febs:form:view")
    public String febsForm() {
        return FebsUtil.view("others/febs/form");
    }

    @GetMapping("febs/form/group")
    @RequiresPermissions("qff:febs:formgroup:view")
    public String febsFormGroup() {
        return FebsUtil.view("others/febs/formGroup");
    }

    @GetMapping("febs/tools")
    @RequiresPermissions("qff:febs:tools:view")
    public String febsTools() {
        return FebsUtil.view("others/febs/tools");
    }

    @GetMapping("febs/icon")
    @RequiresPermissions("qff:febs:icons:view")
    public String febsIcon() {
        return FebsUtil.view("others/febs/icon");
    }

    @GetMapping("febs/others")
    @RequiresPermissions("qff:others:febs:others")
    public String febsOthers() {
        return FebsUtil.view("others/febs/others");
    }

    @GetMapping("apex/line")
    @RequiresPermissions("qff:apex:line:view")
    public String apexLine() {
        return FebsUtil.view("others/apex/line");
    }

    @GetMapping("apex/area")
    @RequiresPermissions("qff:apex:area:view")
    public String apexArea() {
        return FebsUtil.view("others/apex/area");
    }

    @GetMapping("apex/column")
    @RequiresPermissions("qff:apex:column:view")
    public String apexColumn() {
        return FebsUtil.view("others/apex/column");
    }

    @GetMapping("apex/radar")
    @RequiresPermissions("qff:apex:radar:view")
    public String apexRadar() {
        return FebsUtil.view("others/apex/radar");
    }

    @GetMapping("apex/bar")
    @RequiresPermissions("qff:apex:bar:view")
    public String apexBar() {
        return FebsUtil.view("others/apex/bar");
    }

    @GetMapping("apex/mix")
    @RequiresPermissions("qff:apex:mix:view")
    public String apexMix() {
        return FebsUtil.view("others/apex/mix");
    }

    @GetMapping("map")
    @RequiresPermissions("qff:map:view")
    public String map() {
        return FebsUtil.view("others/map/gaodeMap");
    }

    @GetMapping("eximport")
    @RequiresPermissions("qff:others:eximport:view")
    public String eximport() {
        return FebsUtil.view("others/eximport/eximport");
    }

    @GetMapping("eximport/result")
    public String eximportResult() {
        return FebsUtil.view("others/eximport/eximportResult");
    }
}
