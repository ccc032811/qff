package com.neefull.fsp.web.qff.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.common.entity.FebsResponse;
import com.neefull.fsp.web.common.exception.FebsException;
import com.neefull.fsp.web.qff.aspect.Qff;
import com.neefull.fsp.web.qff.entity.*;
import com.neefull.fsp.web.qff.service.IProcessService;
import com.neefull.fsp.web.qff.service.IRecentService;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.system.entity.User;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
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
            Integer count = recentService.addRecent(recent);
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
    @RequiresPermissions("recent:audit")
    public FebsResponse editRecent(Recent recent) throws FebsException {
        try {
            Integer count = recentService.editRecent(recent);
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
    @RequiresPermissions("recent:view")
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
    @RequiresPermissions("recent:del")
    public FebsResponse updateRecentStatus(@PathVariable Integer id) throws FebsException {
        try {
            Recent recent = new Recent();
            recent.setId(id);
            processService.deleteInstance(recent);
            Integer count = recentService.updateRecentStatus(id, ProcessConstant.HAVE_ABNORMAL);
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
    @RequiresPermissions("recent:view")
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
     * @param recent
     * @return
     */
    @GetMapping("/queryHistory")
    public FebsResponse queryHistory(Recent recent) throws FebsException {
        try {
            List<ProcessHistory> list = processService.queryHistory(recent);
            return new FebsResponse().success().data(list);
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
    @RequiresPermissions("recent:audit")
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
    @RequiresPermissions("recent:audit")
    public FebsResponse agreeCurrentProcess(Recent recent) throws FebsException {
        try {
            User user = getCurrentUser();
            List<String> group = processService.getGroupId(recent);
            if(group.contains(user.getUsername())){
                processService.agreeCurrentProcess(recent,user);
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
    @RequiresPermissions("recent:down")
    public void download(Recent recent, HttpServletResponse response) throws FebsException {
        try {
            IPage<RecentExcelImport> recentPage = recentService.getRecentExcelImportPage(recent);
            List<RecentExcelImport> excelImportList = recentPage.getRecords();
            ExcelKit.$Export(RecentExcelImport.class, response).downXlsx(excelImportList, false);
        } catch (Exception e) {
            String message = "导出近效期QFFexcel失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

}
