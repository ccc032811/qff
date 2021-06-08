package com.neefull.fsp.web.qff.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.common.entity.FebsResponse;
import com.neefull.fsp.web.common.entity.QueryRequest;
import com.neefull.fsp.web.common.exception.FebsException;
import com.neefull.fsp.web.qff.aspect.Qff;
import com.neefull.fsp.web.qff.entity.Commodity;
import com.neefull.fsp.web.qff.entity.OtherCommodity;
import com.neefull.fsp.web.qff.entity.ProcessHistory;
import com.neefull.fsp.web.qff.service.ICommodityService;
import com.neefull.fsp.web.qff.service.IProcessService;
import com.neefull.fsp.web.qff.service.impl.CommodityServiceImpl;
import com.neefull.fsp.web.qff.utils.ExcelUtil;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.system.entity.User;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    @RequiresPermissions(value = {"delivery:audit","recent:audit","refund:audit"
            ,"conserve:audit","wrapper:audit","delivery:audit","recent:verify"
            ,"refund:verify", "conserve:verify","wrapper:verify"},logical = Logical.OR)
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
    @RequiresPermissions(value = {"delivery:view","recent:view","refund:view","conserve:view","wrapper:view"},logical = Logical.OR)
    public FebsResponse getCommodityPage(Commodity commodity, QueryRequest request) throws FebsException {
        try {
            IPage<Commodity> pageInfo = commodityService.getCommodityPage(commodity,getCurrentUser(),request);
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
    @RequiresPermissions(value = {"delivery:del","recent:del","refund:del","conserve:del","wrapper:del"},logical = Logical.OR)
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
     * @param id
     * @return
     */
    @GetMapping("/queryHistory/{id}")
    public FebsResponse queryHistory(@PathVariable Integer id) throws FebsException {
        Commodity commodity = new Commodity();
        commodity.setId(id);
        try {
            Map<String, ProcessHistory> map = processService.queryHistory(commodity);
            Commodity commod= commodityService.queryCommodityById(id);
            if(map.size()>=2){
                ProcessHistory processHistory = map.get(ProcessConstant.THREE_STEP);
                processHistory.setDate(commod.getRepTime());
            }
            return new FebsResponse().success().data(map);
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
    @RequiresPermissions(value = {"delivery:audit","recent:audit","refund:audit"
            ,"conserve:audit","wrapper:audit","delivery:audit","recent:verify"
            ,"refund:verify", "conserve:verify","wrapper:verify"},logical = Logical.OR)
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
    @RequiresPermissions(value = {"delivery:audit","recent:audit","refund:audit"
            ,"conserve:audit","wrapper:audit","delivery:audit","recent:verify"
            ,"refund:verify", "conserve:verify","wrapper:verify"},logical = Logical.OR)
    public FebsResponse agreeCurrentProcess(Commodity commodity) throws FebsException {
        try {
            User user = getCurrentUser();
            List<String> group = processService.getGroupId(commodity);
            if(group.contains(user.getUsername())){
                processService.commitProcess(commodity,user);
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
    @RequiresPermissions(value = {"delivery:down","recent:down","refund:down","conserve:down","wrapper:down"},logical = Logical.OR)
    public void download(Commodity commodity, HttpServletResponse response) throws FebsException {
        try {
            List<Commodity> commodityList = commodityService.getPageConserve(commodity,getCurrentUser());
            if(commodity.getStage().equals(ProcessConstant.WRAPPER_NAME)&&CollectionUtils.isNotEmpty(commodityList)){
                List<OtherCommodity> otherCommodityList = new ArrayList<>();
                for (Commodity com : commodityList) {
                    OtherCommodity otherCommodity = new OtherCommodity();
                    BeanUtils.copyProperties(com,otherCommodity);
                    otherCommodityList.add(otherCommodity);
                }
                SXSSFWorkbook workbook = new SXSSFWorkbook();
                SXSSFSheet sheet = workbook.createSheet(commodity.getStage());

                String[] nameList = new String[]{"QFF编号", "Plant工厂", "KDLMaterial物料", "康德乐SAP批次","罗氏物料号","罗氏批号"
                        ,"生产日期","有效期","异常总数","Remark箱号/备注","上报阶段","采购来源","产品分类"
                        ,"投诉编号","回复日期","QFF原因","罗氏QA处理意见","仪器工程师检查结果","备注","变更记录"};
                List<Object[]> dataList = new ArrayList<Object[]>();
                Object[] objs = null;
                for (int i = 0; i < otherCommodityList.size(); i++) {
                    OtherCommodity com = otherCommodityList.get(i);
                    objs = new Object[nameList.length];
                    objs[0] = com.getNumber();
                    objs[1] = com.getPlant();
                    objs[2] = com.getkMater();
                    objs[3] = com.getkBatch();
                    objs[4] = com.getrMater();
                    objs[5] = com.getrBatch();
                    objs[6] = com.getManuDate();
                    objs[7] = com.getExpiryDate();
                    objs[8] = com.getQuarantine();
                    objs[9] = com.getGetRemark();
                    objs[10] = com.getType();
                    objs[11] = com.getSource();
                    objs[12] = com.getClassify();
                    objs[13] = com.getCompNumber();
                    if(StringUtils.isNotEmpty(com.getRepTime())){
                        objs[14] = com.getRepTime().split(" ")[0];
                    }else {
                        objs[14] = com.getRepTime();
                    }
                    objs[15] = com.getReason();
                    objs[16] = com.getrConf();
                    objs[17] = com.getCheckResult();
                    objs[18] = com.getRemark();
                    objs[19] = com.getAlteration();
                    dataList.add(objs);
                }

                ExcelUtil ex = new ExcelUtil(nameList, dataList);
                ex.exportExcel(workbook,sheet,response);

            }else {

                SXSSFWorkbook workbook = new SXSSFWorkbook();
                SXSSFSheet sheet = workbook.createSheet(commodity.getStage());

                String[] nameList = new String[]{"运输单号", "QFF编号", "Plant工厂", "KDLMaterial物料", "康德乐SAP批次","罗氏物料号"
                        ,"是否是危险品","物料描述","罗氏批号","生产日期","有效期","异常总数","单位","BA","上报阶段","采购来源","注册证号"
                        ,"产品分类","Remark箱号/备注","发起日期","回复日期","投诉编号","QFF原因","罗氏QA处理意见","仪器工程师检查结果"
                        ,"备注","变更记录"};
                List<Object[]> dataList = new ArrayList<Object[]>();
                Object[] objs = null;
                for (int i = 0; i < commodityList.size(); i++) {
                    Commodity com = commodityList.get(i);
                    objs = new Object[nameList.length];
                    objs[0] = com.getTransport();
                    objs[1] = com.getNumber();
                    objs[2] = com.getPlant();
                    objs[3] = com.getkMater();
                    objs[4] = com.getkBatch();
                    objs[5] = com.getrMater();
                    objs[6] = com.getIsDanger();
                    objs[7] = com.getpMater();
                    objs[8] = com.getrBatch();
                    objs[9] = com.getManuDate();
                    objs[10] = com.getExpiryDate();
                    objs[11] = com.getQuarantine();
                    objs[12] = com.getrUnit();
                    objs[13] = com.getBa();
                    objs[14] = com.getStage();
                    objs[15] = com.getSource();
                    objs[16] = com.getRegister();
                    objs[17] = com.getClassify();
                    objs[18] = com.getGetRemark();
                    objs[19] = com.getInitDate();
                    if(StringUtils.isNotEmpty(com.getRepTime())){
                        objs[20] = com.getRepTime().split(" ")[0];
                    }else {
                        objs[20] = com.getRepTime();
                    }
                    objs[21] = com.getCompNumber();
                    objs[22] = com.getReason();
                    objs[23] = com.getrConf();
                    objs[24] = com.getCheckResult();
                    objs[25] = com.getRemark();
                    objs[26] = com.getAlteration();
                    dataList.add(objs);
                }

                ExcelUtil ex = new ExcelUtil(nameList, dataList);
                ex.exportExcel(workbook,sheet,response);
            }

        } catch (Exception e) {
            String message = "导出QFF excel失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }


}
