package com.neefull.fsp.web.qff.controller;

import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.common.entity.FebsConstant;
import com.neefull.fsp.web.common.utils.FebsUtil;
import com.neefull.fsp.web.qff.entity.*;
import com.neefull.fsp.web.qff.service.*;
import org.apache.commons.lang3.StringUtils;
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
    private IRocheService rocheService;
    @Autowired
    private IOpinionService opinionService;

    //***************************************************我的代办*****************************************************

    @GetMapping(FebsConstant.VIEW_PREFIX + "qff/mycommission")
    public String showCommission(){
        return FebsUtil.view("index");
    }

    //***********************************************新增***********************************************************

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/textDemo")
    public String commodityDemo(){
        return FebsUtil.view("system/qff/other/commodityDemo");
    }

    //***************************************************到货*****************************************************

    @GetMapping(FebsConstant.VIEW_PREFIX + "delivery/view")
    @RequiresPermissions("delivery:view")
    public String showDelivery(){
        return FebsUtil.view("system/qff/commodity/delivery/delivery");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/deliveryShow/{id}")
    @RequiresPermissions("delivery:view")
    public String getDeliveryShow(@PathVariable Integer id, Model model){
        Commodity commodity = conserveService.queryCommodityById(id);
        model.addAttribute("commodity", commodity);
        return FebsUtil.view("system/qff/commodity/delivery/deliveryShow");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/deliveryAudit/{id}")
    @RequiresPermissions("delivery:view")
    public String getDeliveryAudit(@PathVariable Integer id, Model model){
        Commodity commodity = conserveService.queryCommodityById(id);
        model.addAttribute("commodity", commodity);
        return FebsUtil.view("system/qff/commodity/delivery/deliveryAudit");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/deliveryAlter/{id}/{isShow}")
    @RequiresPermissions("delivery:view")
    public String getDeliveryAlter(@PathVariable Integer id,@PathVariable String isShow, Model model){
        Commodity commodity = conserveService.queryCommodityById(id);
        model.addAttribute("commodity", commodity);
        model.addAttribute("deliveryShow", isShow);
        return FebsUtil.view("system/qff/commodity/delivery/deliveryAudit");
    }

    //******************************************储存 · 分包 · 出库******************************************

    @GetMapping(FebsConstant.VIEW_PREFIX + "conserve/view")
    @RequiresPermissions("conserve:view")
    public String showConserve(){
        return FebsUtil.view("system/qff/commodity/conserve/conserve");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/conserveShow/{id}")
    @RequiresPermissions("conserve:view")
    public String getConserveShow(@PathVariable Integer id, Model model) {
        Commodity commodity = conserveService.queryCommodityById(id);
        model.addAttribute("commodity", commodity);
        return FebsUtil.view("system/qff/commodity/conserve/conserveShow");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/conserveAudit/{id}")
    @RequiresPermissions("conserve:view")
    public String getConserveAudit(@PathVariable Integer id, Model model) {
        Commodity commodity = conserveService.queryCommodityById(id);
        model.addAttribute("commodity", commodity);
        return FebsUtil.view("system/qff/commodity/conserve/conserveAudit");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/conserveAlter/{id}/{isShow}")
    @RequiresPermissions("conserve:view")
    public String getConserveAlter(@PathVariable Integer id,@PathVariable String isShow, Model model) {
        Commodity commodity = conserveService.queryCommodityById(id);
        model.addAttribute("commodity", commodity);
        model.addAttribute("conserveShow", isShow);

        return FebsUtil.view("system/qff/commodity/conserve/conserveAudit");
    }

    //***************************************************其他*****************************************************

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/addQff")
    public String commodityAdd(){
        return FebsUtil.view("system/qff/commodity/wrapper/wrapperAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "wrapper/view")
    @RequiresPermissions("wrapper:view")
    public String showWrapper(){
        return FebsUtil.view("system/qff/commodity/wrapper/wrapper");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/wrapperShow/{id}")
    @RequiresPermissions("wrapper:view")
    public String getWrapperShow(@PathVariable Integer id, Model model) {
        Commodity commodity = conserveService.queryCommodityById(id);
        model.addAttribute("commodity", commodity);
        return FebsUtil.view("system/qff/commodity/wrapper/wrapperShow");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/wrapperAudit/{id}")
    @RequiresPermissions("wrapper:view")
    public String getWrapperAudit(@PathVariable Integer id, Model model) {
        Commodity commodity = conserveService.queryCommodityById(id);
        model.addAttribute("commodity", commodity);
        return FebsUtil.view("system/qff/commodity/wrapper/wrapperAudit");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/wrapperAlter/{id}/{isShow}")
    @RequiresPermissions("wrapper:view")
    public String getWrapperAlter(@PathVariable Integer id,@PathVariable String isShow, Model model) {
        Commodity commodity = conserveService.queryCommodityById(id);
        model.addAttribute("commodity", commodity);
        model.addAttribute("wrapperShow", isShow);

        return FebsUtil.view("system/qff/commodity/wrapper/wrapperAudit");
    }


    //***********************************************退货***********************************************************


    @GetMapping(FebsConstant.VIEW_PREFIX + "/refund/view")
    @RequiresPermissions("refund:view")
    public String showRefund(){
        return FebsUtil.view("system/qff/commodity/refund/refund");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/refund/refundShow/{id}")
    @RequiresPermissions("refund:view")
    public String getRefundShow(@PathVariable Integer id, Model model) {
        Commodity commodity = conserveService.queryCommodityById(id);
        model.addAttribute("commodity", commodity);
        return FebsUtil.view("system/qff/commodity/refund/refundShow");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/refund/refundAudit/{id}")
    @RequiresPermissions("refund:view")
    public String getRefundAudit(@PathVariable Integer id, Model model) {
        Commodity commodity = conserveService.queryCommodityById(id);
        model.addAttribute("commodity", commodity);
        return FebsUtil.view("system/qff/commodity/refund/refundAudit");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/refund/refundAlter/{id}/{isShow}")
    @RequiresPermissions("refund:view")
    public String getRefundAlter(@PathVariable Integer id,@PathVariable String isShow, Model model) {
        Commodity commodity = conserveService.queryCommodityById(id);
        model.addAttribute("commodity", commodity);
        model.addAttribute("refundShow", isShow);

        return FebsUtil.view("system/qff/commodity/refund/refundAudit");
    }


    //***************************************************近效期*****************************************************

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/recent/addRecent")
    public String recentAdd(){
        return FebsUtil.view("system/qff/recent/recentAdd");
    }

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

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/recent/recentAlter/{id}/{isShow}")
    @RequiresPermissions("recent:view")
    public String getRecentAlter(@PathVariable Integer id,@PathVariable String isShow, Model model) {
        Recent recent = recentService.queryRecentById(id);
        model.addAttribute("recent", recent);
        model.addAttribute("recentShow", isShow);

        return FebsUtil.view("system/qff/recent/recentAudit");
    }

    //***********************************************罗氏内部发起***********************************************************

    @GetMapping(FebsConstant.VIEW_PREFIX + "roche/view")
    @RequiresPermissions("roche:view")
    public String showRoche(){
        return FebsUtil.view("system/qff/roche/roche");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/roche/add")
    public String rocheCommit(){
        return FebsUtil.view("system/qff/roche/rocheCommit");
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

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/roche/rocheAlter/{id}/{isShow}")
    @RequiresPermissions("roche:view")
    public String getRocheAudit(@PathVariable Integer id,@PathVariable String isShow, Model model) {
        Roche roche = rocheService.queryRocheById(id);
        model.addAttribute("roche", roche);
        model.addAttribute("rocheShow", isShow);

        return FebsUtil.view("system/qff/roche/rocheAudit");
    }

    //***********************************************国际到货超温，无温度计***********************************************************


    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/temperature/addTemperature")
    public String temperatureAdd(){
        return FebsUtil.view("system/qff/temperature/temperatureAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "temperature/view")
    @RequiresPermissions("temperature:view")
    public String showTemperature(){
        return FebsUtil.view("system/qff/temperature/temperature");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/temperature/temperatureShow/{id}")
    @RequiresPermissions("temperature:view")
    public String getTemperatureShow(@PathVariable Integer id, Model model){
        Recent recent = recentService.queryRecentById(id);
        model.addAttribute("temperature",recent);
        return FebsUtil.view("system/qff/temperature/temperatureShow");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/temperature/temperatureAudit/{id}")
    @RequiresPermissions("temperature:view")
    public String getTemperatureAudit(@PathVariable Integer id, Model model){
        Recent recent = recentService.queryRecentById(id);
        model.addAttribute("temperature",recent);
        return FebsUtil.view("system/qff/temperature/temperatureAudit");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/temperature/temperatureAlter/{id}/{isShow}")
    @RequiresPermissions("temperature:view")
    public String getTemperatureAlter(@PathVariable Integer id,@PathVariable String isShow, Model model) {
        Recent recent = recentService.queryRecentById(id);
        model.addAttribute("temperature", recent);
        model.addAttribute("temperatureShow", isShow);

        return FebsUtil.view("system/qff/temperature/temperatureAudit");
    }

    //***********************************************字典管理***********************************************************

    @GetMapping(FebsConstant.VIEW_PREFIX + "opinion/view")
    public String showOpinion(){
        return FebsUtil.view("system/qff/opinion/opinion");
    }

    //***********************************************系统日志***********************************************************

    @GetMapping(FebsConstant.VIEW_PREFIX + "/qffLog/view")
    public String showQffLog(){
        return FebsUtil.view("system/qff/log/qffLog");
    }




    //***************************************************出库*****************************************************
//
//    @GetMapping(FebsConstant.VIEW_PREFIX + "export/view")
//    @RequiresPermissions("export:view")
//    public String showExport(){
//        return FebsUtil.view("system/qff/commodity/export/export");
//    }
//
//    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/exportShow/{id}")
//    @RequiresPermissions("export:view")
//    public String getExportShow(@PathVariable Integer id, Model model) {
//        Commodity commodity = conserveService.queryCommodityById(id);
//        model.addAttribute("commodity", commodity);
//        return FebsUtil.view("system/qff/commodity/export/exportShow");
//    }
//
//    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/exportAudit/{id}")
//    @RequiresPermissions("export:view")
//    public String getExportAudit(@PathVariable Integer id, Model model) {
//        Commodity commodity = conserveService.queryCommodityById(id);
//        model.addAttribute("commodity", commodity);
//        return FebsUtil.view("system/qff/commodity/export/exportAudit");
//    }
//
//    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/exportAlter/{id}/{isShow}")
//    @RequiresPermissions("export:view")
//    public String getExportAlter(@PathVariable Integer id,@PathVariable String isShow, Model model) {
//        Commodity commodity = conserveService.queryCommodityById(id);
//        model.addAttribute("commodity", commodity);
//        model.addAttribute("exportShow", isShow);
//
//        return FebsUtil.view("system/qff/commodity/export/exportAudit");
//    }


    //    //***************************************************分包*****************************************************
//
//    @GetMapping(FebsConstant.VIEW_PREFIX + "package/view")
//    @RequiresPermissions("package:view")
//    public String showPackage(){
//        return FebsUtil.view("system/qff/commodity/package/package");
//    }
//
//    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/packageShow/{id}")
//    @RequiresPermissions("package:view")
//    public String getPackageShow(@PathVariable Integer id, Model model) {
//        Commodity commodity = conserveService.queryCommodityById(id);
//        model.addAttribute("commodity", commodity);
//        return FebsUtil.view("system/qff/commodity/package/packageShow");
//    }
//
//    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/packageAudit/{id}")
//    @RequiresPermissions("package:view")
//    public String getPackageAudit(@PathVariable Integer id, Model model) {
//        Commodity commodity = conserveService.queryCommodityById(id);
//        model.addAttribute("commodity", commodity);
//        return FebsUtil.view("system/qff/commodity/package/packageAudit");
//    }
//
//    @GetMapping(FebsConstant.VIEW_PREFIX + "system/qff/commodity/packageAlter/{id}/{isShow}")
//    @RequiresPermissions("package:view")
//    public String getPackageAlter(@PathVariable Integer id,@PathVariable String isShow, Model model) {
//        Commodity commodity = conserveService.queryCommodityById(id);
//        model.addAttribute("commodity", commodity);
//        model.addAttribute("packageShow", isShow);
//
//        return FebsUtil.view("system/qff/commodity/package/packageAudit");
//    }



}
