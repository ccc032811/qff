package com.neefull.fsp.web.qff.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.common.entity.FebsResponse;
import com.neefull.fsp.web.common.exception.FebsException;
import com.neefull.fsp.web.qff.aspect.Qff;
import com.neefull.fsp.web.qff.entity.Commodity;
import com.neefull.fsp.web.qff.entity.ProcessHistory;
import com.neefull.fsp.web.qff.service.ICommodityService;
import com.neefull.fsp.web.qff.service.IProcessService;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.system.entity.User;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 *新增到货养护包装QFF
 * @Author: chengchengchu
 * @Date: 2019/12/6  18:50
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/commodity")
public class CommodityController extends BaseController {

    @Autowired
    private ICommodityService commodityService;
    @Autowired
    private IProcessService processService;


    /**新增QFF
     * @param commodity
     * @return
     * @throws FebsException
     */
    @Qff("新增QFF")
    @PostMapping("/add")
    public FebsResponse addCommodity(Commodity commodity) throws FebsException {
        try {
            commodityService.addCommodity(commodity);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "新增QFF失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    /**更新QFF
     * @param commodity
     * @return
     * @throws FebsException
     */
    @Qff("更新QFF")
    @PostMapping("/edit")
    @RequiresPermissions(value = {"delivery:audit","recent:audit","refund:audit","conserve:audit","conserve:audit","package:audit"},logical = Logical.OR)
    public FebsResponse editCommodity(Commodity commodity) throws FebsException {
        try {
            commodityService.editCommodity(commodity);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "更新QFF失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    /**修改流程中的QFF
     * @param commodity
     * @return
     */
    @Qff("修改QFF")
    @PostMapping("/alter")
    public FebsResponse alterCommodity(Commodity commodity){
        processService.alterCommodity(commodity,getCurrentUser());
        return new FebsResponse().success();
    }

    /**查询QFF
     * @param commodity
     * @return
     */
    @GetMapping("/list")
    @RequiresPermissions(value = {"delivery:view","recent:view","refund:view","conserve:view","conserve:view","package:view"},logical = Logical.OR)
    public FebsResponse getCommodityPage(Commodity commodity) throws FebsException {
        try {
            IPage<Commodity> pageInfo = commodityService.getCommodityPage(commodity,getCurrentUser());
            Map<String, Object> dataTable = getDataTable(pageInfo);
            return new FebsResponse().success().data(dataTable);
        } catch (Exception e) {
            String message = "查询QFF失败";
            log.error(message,e);
            throw new FebsException(message);
    }

    }

    /**删除QFF
     * @param id
     * @return
     */
    @Qff("删除QFF")
    @GetMapping("/deleteCommodity/{id}")
    @RequiresPermissions(value = {"delivery:del","recent:del","refund:del","conserve:del","conserve:del","package:del"},logical = Logical.OR)
    public FebsResponse updateCommodityStatus(@PathVariable Integer id) throws FebsException {
        try {
            Commodity commodity = new Commodity();
            commodity.setId(id);
            processService.deleteInstance(commodity);
            commodityService.updateCommodityStatus(id, ProcessConstant.HAVE_ABNORMAL);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "删除QFF失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    /**根据QFF ID查询
     * @param id
     * @return
     * @throws FebsException
     */
    @GetMapping("/queryCommodity/{id}")
    public FebsResponse queryCommodityById(@PathVariable Integer id) throws FebsException {
        try {
            Commodity commodity = commodityService.queryCommodityById(id);
            return new FebsResponse().success().data(commodity);
        } catch (Exception e) {
            String message = "根据QFF ID查询失败";
            log.error(message,e);
            throw new FebsException(message);
        }

    }

    /**查询QFF流程
     * @param commodity
     * @return
     */
    @GetMapping("/queryHistory/{id}")
    public FebsResponse queryHistory(@PathVariable Integer id) throws FebsException {
        Commodity commodity = new Commodity();
        commodity.setId(id);
        try {
            List<ProcessHistory> list = processService.queryHistory(commodity);
            Commodity commod= commodityService.queryCommodityById(id);
            if(list.size()>=2){
                ProcessHistory processHistory = list.get(1);
                processHistory.setDate(commod.getRepTime());
            }
            return new FebsResponse().success().data(list);
        } catch (Exception e) {
            String message = "查询QFF流程";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    /**提交QFF流程
     * @param commodity
     * @return
     * @throws FebsException
     */
    @Qff("提交QFF流程")
    @PostMapping("/commit")
    @RequiresPermissions(value = {"delivery:audit","recent:audit","refund:audit","conserve:audit","conserve:audit","package:audit"},logical = Logical.OR)
    public FebsResponse commitProcess(Commodity commodity) throws FebsException {

        try {
            User user = getCurrentUser();
            processService.commitProcess(commodity,user);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "提交QFF流程失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    /**同意当前QFF任务
     * @param commodity
     * @return
     * @throws FebsException
     */
    @Qff("同意当前QFF任务")
    @PostMapping("/agree")
    @RequiresPermissions(value = {"delivery:audit","recent:audit","refund:audit","conserve:audit","conserve:audit","package:audit"},logical = Logical.OR)
    public FebsResponse agreeCurrentProcess(Commodity commodity) throws FebsException {
        try {
            User user = getCurrentUser();
            List<String> group = processService.getGroupId(commodity);
            if(group.contains(user.getUsername())){
                processService.agreeCurrentProcess(commodity,user);
            }else {
                throw new FebsException("当前无权限或改数据已审核");
            }
            return new FebsResponse().success();
        } catch (FebsException e) {
            String message = "同意当前QFF任务失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    /**导出QFF excel
     * @param commodity
     * @param response
     */
    @GetMapping("excel")
    @RequiresPermissions(value = {"delivery:down","recent:down","refund:down","conserve:down","conserve:down","package:down"},logical = Logical.OR)
    public void download(Commodity commodity, HttpServletResponse response) throws FebsException {
        try {
            List<Commodity> commodityList = commodityService.getPageConserve(commodity,getCurrentUser());
            for (Commodity com : commodityList) {
                if(com.getManuDate()!=null){
                    com.setManuDate(com.getManuDate().replace("-","/"));
                }
                if(com.getExpiryDate()!=null){
                    com.setExpiryDate(com.getExpiryDate().replace("-","/"));
                }
                if(com.getInitDate()!=null){
                    com.setInitDate(com.getInitDate().replace("-","/"));
                }
                if(com.getRepTime()!=null){
                    com.setRepTime(com.getRepTime().replace("-","/"));
                }

            }
            ExcelKit.$Export(Commodity.class, response).downXlsx(commodityList, false);
        } catch (Exception e) {
            String message = "导出QFF excel失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }


}
