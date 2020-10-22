package com.neefull.fsp.web.qff.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neefull.fsp.common.util.DateUtils;
import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.common.entity.FebsResponse;
import com.neefull.fsp.web.common.exception.FebsException;
import com.neefull.fsp.web.qff.aspect.Qff;
import com.neefull.fsp.web.qff.entity.*;
import com.neefull.fsp.web.qff.service.IProcessService;
import com.neefull.fsp.web.qff.service.IRecentService;
import com.neefull.fsp.web.qff.utils.ExcelUtil;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.system.entity.User;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 近效期QFF操作
 *
 * @Author: chengchengchu
 * @Date: 2019/11/29  16:09
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/recent")
public class RecentController extends BaseController {


    @Autowired
    private IRecentService recentService;
    @Autowired
    private IProcessService processService;

    /**新增近效期QFF
     * @param recent
     * @return
     * @throws FebsException
     */
    @Qff("新增近效期QFF")
    @PostMapping("/add")
    public FebsResponse addRecent(Recent recent) throws FebsException {
        try {
            recentService.addRecent(recent);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "新增近效期QFF失败";
            log.error(message,e);
            throw new FebsException(message);

        }
    }

    /**更新近效期QFF
     * @param recent
     * @return
     * @throws FebsException
     */
    @Qff("更新近效期QFF")
    @PostMapping("/edit")
    @RequiresPermissions(value = {"recent:audit","temperature:audit"},logical = Logical.OR)
    public FebsResponse editRecent(Recent recent) throws FebsException {
        try {
            recentService.editRecent(recent);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "更新近效期QFF失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    @Qff("修改近效期QFF")
    @PostMapping("/alter")
    public FebsResponse alterRecent(Recent recent){
        processService.alterRecent(recent,getCurrentUser());
        return new FebsResponse().success();
    }

    /**查询近效期QFF
     * @param recent
     * @return
     */
    @GetMapping("/list")
    @RequiresPermissions(value = {"recent:view","temperature:view"},logical = Logical.OR)
    public FebsResponse getRecentPage(Recent recent) throws FebsException {
        try {
            IPage<Recent> pageInfo = recentService.getRecentPage(recent,getCurrentUser());
            Map<String, Object> dataTable = getDataTable(pageInfo);
            return new FebsResponse().success().data(dataTable);
        } catch (Exception e) {
            String message = "查询近效期QFF失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    /**删除近效期QFF
     * @param id
     * @return
     * @throws FebsException
     */
    @Qff("删除近效期QFF")
    @GetMapping("/deleteRecent/{id}")
    @RequiresPermissions(value = {"recent:del","temperature:del"},logical = Logical.OR)
    public FebsResponse updateRecentStatus(@PathVariable Integer id) throws FebsException {
        try {
            Recent recent = new Recent();
            recent.setId(id);
            processService.deleteInstance(recent);
            recentService.updateRecentStatus(id, ProcessConstant.HAVE_ABNORMAL);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "删除近效期QFF失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    /**查询近效期QFF
     * @param id
     * @return
     * @throws FebsException
     */
    @GetMapping("/queryRecent/{id}")
    @RequiresPermissions(value = {"recent:view","temperature:view"},logical = Logical.OR)
    public FebsResponse queryRecentById(@PathVariable Integer id) throws FebsException {
        try {
            Recent recent = recentService.queryRecentById(id);
            return new FebsResponse().success().data(recent);
        } catch (Exception e) {
            String message = "查询近效期QFF失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }


    /**查询流程
     * @param
     * @return
     */
    @GetMapping("/queryHistory/{id}")
    public FebsResponse queryHistory(@PathVariable Integer id) throws FebsException {
        Recent recent = new Recent();
        recent.setId(id);
        try {
            Map<String, ProcessHistory> map = processService.queryHistory(recent);
            Recent rec = recentService.queryRecentById(id);
            if(map.size()>=2){
                ProcessHistory processHistory = map.get(ProcessConstant.THREE_STEP);
                processHistory.setDate(rec.getRepDate());
            }
            return new FebsResponse().success().data(map);
        } catch (Exception e) {
            String message = "查询近效期QFF流程失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    /**提交流程
     * @param recent
     * @return
     * @throws FebsException
     */
    @Qff("提交近效期QFF流程")
    @PostMapping("/commit")
    @RequiresPermissions(value = {"recent:audit","temperature:audit"},logical = Logical.OR)
    public FebsResponse commitProcess(Recent recent) throws FebsException {
        try {
            User user = getCurrentUser();
            processService.commitProcess(recent,user);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "提交近效期QFF流程失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    /**同意当前任务
     * @param recent
     * @return
     * @throws FebsException
     */
    @Qff("同意近效期QFF任务")
    @PostMapping("/agree")
    @RequiresPermissions(value = {"recent:audit","temperature:audit"},logical = Logical.OR)
    public FebsResponse agreeCurrentProcess(Recent recent) throws FebsException {
        try {
            User user = getCurrentUser();
            List<String> group = processService.getGroupId(recent);
            if(group.contains(user.getUsername())){
                processService.commitProcess(recent,user);
            }else {
                throw new FebsException("当前无权限或改数据已审核");
            }
            return new FebsResponse().success();
        } catch (FebsException e) {
            String message = "同意近效期QFF任务失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    /**导出excel
     * @param recent
     * @param response
     */
    @GetMapping("excel")
    @RequiresPermissions(value = {"recent:down","temperature:down"},logical = Logical.OR)
    public void download(Recent recent, HttpServletResponse response) throws FebsException {
        try {
            List<Recent> recentList = recentService.getRecentExcelImportPage(recent,getCurrentUser());
            if(recent.getStage().equals(ProcessConstant.RECENT_NAME)){
                SXSSFWorkbook workbook = new SXSSFWorkbook();
                SXSSFSheet sheet = workbook.createSheet(recent.getStage());
                String[] nameList = new String[]{"开始时间", "工厂","产品信息","总数量"
                        ,"罗氏QA处理意见","回复日期","变更记录"};
                List<Object[]> dataList = new ArrayList<Object[]>();
                Object[] objs = null;
                for (int i = 0; i < recentList.size(); i++) {
                    Recent rec = recentList.get(i);
                    objs = new Object[nameList.length];
                    if(StringUtils.isNotEmpty(rec.getStartDate())){
                        objs[0] = rec.getStartDate().split(" ")[0];
                    }else {
                        objs[0] = rec.getStartDate();
                    }
                    objs[1] = rec.getFactory();
                    objs[2] = rec.getMessage();
                    objs[3] = rec.getCount();
                    objs[4] = rec.getrConf();
                    if(StringUtils.isNotEmpty(rec.getRepDate())) {
                        objs[5] = rec.getRepDate().split(" ")[0];
                    }else {
                        objs[5] = rec.getRepDate();
                    }
                    objs[6] = rec.getAlteration();
                    dataList.add(objs);
                }
                ExcelUtil ex = new ExcelUtil(nameList, dataList);
                ex.exportExcel(workbook,sheet,response);

            }else if(recent.getStage().equals(ProcessConstant.TEMPERATURE_NAME)){
                List<Temperature> temperatures = new ArrayList<>();
                for (Recent rec : recentList) {
                    Temperature temperature = new Temperature();
                    BeanUtils.copyProperties(rec,temperature);
                    temperatures.add(temperature);
                }
                SXSSFWorkbook workbook = new SXSSFWorkbook();
                SXSSFSheet sheet = workbook.createSheet(recent.getStage());
                String[] nameList = new String[]{"QFF编号", "事件描述", "开始时间" ,"产品信息","总数量"
                        ,"罗氏QA处理意见","回复日期","变更记录"};
                List<Object[]> dataList = new ArrayList<Object[]>();
                Object[] objs = null;
                for (int i = 0; i < temperatures.size(); i++) {
                    Temperature tem = temperatures.get(i);
                    objs = new Object[nameList.length];
                    objs[0] = tem.getNumber();
                    objs[1] = tem.getRemark();
                    if(StringUtils.isNotEmpty(tem.getStartDate())){
                        objs[2] = tem.getStartDate().split(" ")[0];
                    }else {
                        objs[2] = tem.getStartDate();
                    }
                    objs[3] = tem.getMessage();
                    objs[4] = tem.getCount();
                    objs[5] = tem.getrConf();
                    if(StringUtils.isNotEmpty(tem.getRepDate())) {
                        objs[6] = tem.getRepDate().split(" ")[0];
                    }else {
                        objs[6] = tem.getRepDate();
                    }
                    objs[7] = tem.getAlteration();
                    dataList.add(objs);
                }
                ExcelUtil ex = new ExcelUtil(nameList, dataList);
                ex.exportExcel(workbook,sheet,response);
            }
        } catch (Exception e) {
            String message = "导出近效期QFFexcel失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

}
