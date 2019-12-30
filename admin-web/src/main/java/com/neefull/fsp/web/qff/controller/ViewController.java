package com.neefull.fsp.web.qff.controller;

import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.common.entity.FebsConstant;
import com.neefull.fsp.web.common.utils.FebsUtil;
import com.neefull.fsp.web.qff.entity.*;
import com.neefull.fsp.web.qff.service.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @Author: chengchengchu
 * @Date: 2019/12/13  11:12
 */

@Controller("/qffView")
public class ViewController extends BaseController {

    @Autowired
    private ICommodityService conserveService;
    @Autowired
    private IRecentService recentService;
    @Autowired
    private IRefundService refundService;
    @Autowired
    private IRocheService rocheService;
    @Autowired
    private IOpinionService opinionService;


    //***************************************************到货*****************************************************

    @GetMapping(FebsConstant.VIEW_PREFIX + "delivery/view")
    @RequiresPermissions("commodity:view")
    public String showDelivery(){
        return FebsUtil.view("system/qff/commodity/delivery");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/deliveryShow/{id}")
    @RequiresPermissions("commodity:view")
    public String getDeliveryShow(@PathVariable Integer id, Model model){
        Commodity commodity = conserveService.queryCommodityById(id);
        model.addAttribute("commodity", commodity);
        return FebsUtil.view("system/qff/commodity/deliveryShow");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/deliveryAudit/{id}")
    @RequiresPermissions("commodity:view")
    public String getDeliveryAudit(@PathVariable Integer id, Model model){
        Commodity commodity = conserveService.queryCommodityById(id);
        model.addAttribute("commodity", commodity);
        return FebsUtil.view("system/qff/commodity/deliveryAudit");
    }

    //***************************************************养护*****************************************************

    @GetMapping(FebsConstant.VIEW_PREFIX + "conserve/view")
    @RequiresPermissions("commodity:view")
    public String showConserve(){
        return FebsUtil.view("system/qff/commodity/conserve");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/conserveShow/{id}")
    @RequiresPermissions("commodity:view")
    public String getConserveShow(@PathVariable Integer id, Model model) {
        Commodity commodity = conserveService.queryCommodityById(id);
        model.addAttribute("commodity", commodity);
        return FebsUtil.view("system/qff/commodity/conserveShow");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/conserveAudit/{id}")
    @RequiresPermissions("commodity:view")
    public String getConserveAudit(@PathVariable Integer id, Model model) {
        Commodity commodity = conserveService.queryCommodityById(id);
        model.addAttribute("commodity", commodity);
        return FebsUtil.view("system/qff/commodity/conserveAudit");
    }

    //***************************************************分包*****************************************************

    @GetMapping(FebsConstant.VIEW_PREFIX + "wrapper/view")
    @RequiresPermissions("commodity:view")
    public String showWrapper(){
        return FebsUtil.view("system/qff/commodity/wrapper");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/wrapperShow/{id}")
    @RequiresPermissions("commodity:view")
    public String getWrapperShow(@PathVariable Integer id, Model model) {
        Commodity commodity = conserveService.queryCommodityById(id);
        model.addAttribute("commodity", commodity);
        return FebsUtil.view("system/qff/commodity/wrapperShow");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/wrapperAudit/{id}")
    @RequiresPermissions("commodity:view")
    public String getWrapperAudit(@PathVariable Integer id, Model model) {
        Commodity commodity = conserveService.queryCommodityById(id);
        model.addAttribute("commodity", commodity);
        return FebsUtil.view("system/qff/commodity/wrapperAudit");
    }

    //***************************************************出库*****************************************************

    @GetMapping(FebsConstant.VIEW_PREFIX + "export/view")
    @RequiresPermissions("commodity:view")
    public String showExport(){
        return FebsUtil.view("system/qff/commodity/export");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/exportShow/{id}")
    @RequiresPermissions("commodity:view")
    public String getExportShow(@PathVariable Integer id, Model model) {
        Commodity commodity = conserveService.queryCommodityById(id);
        model.addAttribute("commodity", commodity);
        return FebsUtil.view("system/qff/commodity/exportShow");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/exportAudit/{id}")
    @RequiresPermissions("commodity:view")
    public String getExportAudit(@PathVariable Integer id, Model model) {
        Commodity commodity = conserveService.queryCommodityById(id);
        model.addAttribute("commodity", commodity);
        return FebsUtil.view("system/qff/commodity/exportAudit");
    }

    //***************************************************近效期*****************************************************

    @GetMapping(FebsConstant.VIEW_PREFIX + "recent/view")
    @RequiresPermissions("recent:view")
    public String showRecent(){
        return FebsUtil.view("system/qff/recent/recent");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/recent/recentShow/{id}")
    @RequiresPermissions("recent:view")
    public String getRecentShow(@PathVariable Integer id, Model model){
        Recent recent = recentService.queryRecentById(id);
        model.addAttribute("recent",recent);
        return FebsUtil.view("system/qff/recent/recentShow");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/recent/recentAudit/{id}")
    @RequiresPermissions("recent:view")
    public String getRecentAudit(@PathVariable Integer id, Model model){
        Recent recent = recentService.queryRecentById(id);
        model.addAttribute("recent",recent);
        return FebsUtil.view("system/qff/recent/recentAudit");
    }

    //***********************************************退货***********************************************************

    @GetMapping(FebsConstant.VIEW_PREFIX + "/refund/view")
    @RequiresPermissions("refund:view")
    public String showRefund(){
        return FebsUtil.view("system/qff/refund/refund");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/refund/refundShow/{id}")
    @RequiresPermissions("refund:view")
    public String getRefundShow(@PathVariable Integer id, Model model) {
        Refund refund = refundService.queryRefundById(id);
        model.addAttribute("refund", refund);
        return FebsUtil.view("system/qff/refund/refundShow");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/refund/refundAudit/{id}")
    @RequiresPermissions("refund:view")
    public String getRefundAudit(@PathVariable Integer id, Model model) {
        Refund refund = refundService.queryRefundById(id);
        model.addAttribute("refund", refund);
        return FebsUtil.view("system/qff/refund/refundAudit");
    }

    //***********************************************罗氏内部发起***********************************************************

    @GetMapping(FebsConstant.VIEW_PREFIX + "roche/view")
    @RequiresPermissions("roche:view")
    public String showRoche(){
        return FebsUtil.view("system/qff/roche/roche");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/roche/rocheShow/{id}")
    @RequiresPermissions("roche:view")
    public String getRocheShow(@PathVariable Integer id, Model model) {
        Roche roche = rocheService.queryRocheById(id);
        model.addAttribute("roche", roche);
        return FebsUtil.view("system/qff/roche/rocheShow");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/roche/rocheAudit/{id}")
    @RequiresPermissions("roche:view")
    public String getRocheAudit(@PathVariable Integer id, Model model) {
        Roche roche = rocheService.queryRocheById(id);
        model.addAttribute("roche", roche);
        return FebsUtil.view("system/qff/roche/rocheAudit");
    }

    //***********************************************字典管理***********************************************************

    @GetMapping(FebsConstant.VIEW_PREFIX + "opinion/view")
    public String showOpinion(){
        return FebsUtil.view("system/qff/opinion/opinion");
    }



}