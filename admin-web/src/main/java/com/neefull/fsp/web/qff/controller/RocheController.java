package com.neefull.fsp.web.qff.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.common.entity.FebsResponse;
import com.neefull.fsp.web.common.exception.FebsException;
import com.neefull.fsp.web.qff.aspect.Qff;
import com.neefull.fsp.web.qff.entity.ProcessHistory;
import com.neefull.fsp.web.qff.entity.Roche;
import com.neefull.fsp.web.qff.service.IProcessService;
import com.neefull.fsp.web.qff.service.IRocheService;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.system.entity.User;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 罗氏内部发起QFF操作
 *
 * @Author: chengchengchu
 * @Date: 2019/11/29  16:11
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/roche")
public class RocheController extends BaseController {


    @Autowired
    private IRocheService rocheService;
    @Autowired
    private IProcessService processService;

    /**新增罗氏内部发起QFF
     * @param roche
     * @return
     * @throws FebsException
     */
    @Qff("新增罗氏内部发起QFF")
    @PostMapping("/add")
    public FebsResponse addRoche(Roche roche) throws FebsException {
        try {
            rocheService.addRoche(roche);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "新增罗氏内部QFF失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    /**更新罗氏内部发起QFF
     * @param roche
     * @return
     * @throws FebsException
     */
    @Qff("更新罗氏内部发起QFF")
    @PostMapping("/edit")
    @RequiresPermissions("roche:audit")
    public FebsResponse editRoche(Roche roche) throws FebsException {
        try {
            rocheService.editRoche(roche);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "更新罗氏内部QFF失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    @Qff("修改罗氏发起QFF")
    @PostMapping("/alter")
    public FebsResponse alterRoche(Roche roche){
        processService.alterRoche(roche,getCurrentUser());
        return new FebsResponse().success();
    }


    /**查询罗氏内部发起QFF
     * @param roche
     * @return
     */
    @GetMapping("/list")
    @RequiresPermissions("roche:view")
    public FebsResponse getRochePage(Roche roche) throws FebsException {
        try {
            IPage<Roche> pageInfo = rocheService.getRochePage(roche,getCurrentUser());
            Map<String, Object> dataTable = getDataTable(pageInfo);
            return new FebsResponse().success().data(dataTable);
        } catch (Exception e) {
            String message = "新查询罗氏内部QFF失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    /**删除罗氏内部QFF
     * @param id
     * @return
     * @throws FebsException
     */
    @Qff("删除罗氏内部QFF")
    @GetMapping("/deleteRoche/{id}")
    @RequiresPermissions("roche:del")
    public FebsResponse updateRocheStatus(@PathVariable Integer id) throws FebsException {
        try {
            Roche roche = new Roche();
            roche.setId(id);
            processService.deleteInstance(roche);
            rocheService.updateRocheStatus(id, ProcessConstant.HAVE_ABNORMAL);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "删除罗氏内部QFF失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    /**查询罗氏内部QFF
     * @param id
     * @return
     * @throws FebsException
     */
    @GetMapping("/queryRoche")
    @RequiresPermissions("roche:view")
    public FebsResponse queryRocheById(Integer id) throws FebsException {
        try {
            Roche roche = rocheService.queryRocheById(id);
            return new FebsResponse().success().data(roche);
        } catch (Exception e) {
            String message = "查询罗氏内部QFF失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }


    /**查询流程
     * @param id
     * @return
     */
    @GetMapping("/queryHistory/{id}")
    public FebsResponse queryHistory(@PathVariable Integer id) throws FebsException {
        Roche roche = new Roche();
        roche.setId(id);
        try {
            List<ProcessHistory> list = processService.queryHistory(roche);
            Roche roc = rocheService.queryRocheById(id);
            if(list.size()>=1){
                ProcessHistory processHistory = list.get(0);
                processHistory.setDate(DateFormatUtils.format(roc.getCreateTime(),"yyyy-MM-dd HH:mm:ss"));
            }
            return new FebsResponse().success().data(list);
        } catch (Exception e) {
            String message = "查询罗氏内部QFF流程失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    /**提交流程
     * @param roche
     * @return
     * @throws FebsException
     */
    @Qff("提交罗氏内部QFF流程")
    @PostMapping("/commit")
    @RequiresPermissions("roche:audit")
    public FebsResponse commitProcess(Roche roche) throws FebsException {
        try {
            User user = getCurrentUser();
            processService.commitProcess(roche,user);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "提交罗氏内部QFF流程失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    /**同意当前任务
     * @param roche
     * @return
     * @throws FebsException
     */
    @Qff("同意罗氏内部QFF任务")
    @PostMapping("/agree")
    @RequiresPermissions("roche:audit")
    public FebsResponse agreeCurrentProcess(Roche roche) throws FebsException {
        try {
            User user = getCurrentUser();
            List<String> group = processService.getGroupId(roche);
            if(group.contains(user.getUsername())){
                processService.commitProcess(roche,user);
//                processService.agreeCurrentProcess(roche,user,null);
            }else {
                throw new FebsException("当前无权限或改数据已审核");
            }
            return new FebsResponse().success();
        } catch (FebsException e) {
            String message = "同意罗氏内部QFF流程失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

    /**导出excel
     * @param roche
     * @param response
     */
    @GetMapping("excel")
    @RequiresPermissions("roche:down")
    public void download(Roche roche, HttpServletResponse response) throws FebsException {
        try {
            List<Roche> rocheList = rocheService.getRocheExcelPage(roche,getCurrentUser());

            ExcelKit.$Export(Roche.class, response).downXlsx(rocheList, false);
        } catch (Exception e) {
            String message = "导出罗氏内部QFFexcel失败";
            log.error(message,e);
            throw new FebsException(message);
        }
    }

}
