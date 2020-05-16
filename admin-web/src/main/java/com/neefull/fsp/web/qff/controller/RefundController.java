//package com.neefull.fsp.web.qff.controller;
//
//import com.baomidou.mybatisplus.core.metadata.IPage;
//import com.neefull.fsp.web.common.controller.BaseController;
//import com.neefull.fsp.web.common.entity.FebsResponse;
//import com.neefull.fsp.web.common.exception.FebsException;
//import com.neefull.fsp.web.qff.aspect.Qff;
//import com.neefull.fsp.web.qff.entity.ProcessHistory;
//import com.neefull.fsp.web.qff.entity.Refund;
//import com.neefull.fsp.web.qff.service.IProcessService;
//import com.neefull.fsp.web.qff.service.IRefundService;
//import com.neefull.fsp.web.qff.utils.ProcessConstant;
//import com.neefull.fsp.web.system.entity.User;
//import com.wuwenze.poi.ExcelKit;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.shiro.authz.annotation.RequiresPermissions;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.HttpServletResponse;
//import java.util.List;
//import java.util.Map;
//
///**
// * 退货QFF操作
// *
// * @Author: chengchengchu
// * @Date: 2019/11/29  16:12
// */
//@Slf4j
//@Validated
//@RestController
//@RequestMapping("/refund")
//public class RefundController extends BaseController {
//
//
//    @Autowired
//    private IRefundService refundService;
//    @Autowired
//    private IProcessService processService;
//
//    /**新增退货QFF
//     * @param refund
//     * @return
//     * @throws FebsException
//     */
//    @Qff("新增退货QFF")
//    @PostMapping("/add")
//    public FebsResponse addRefund(Refund refund) throws FebsException {
//        try {
//            Integer count = refundService.addRefund(refund);
//            return new FebsResponse().success();
//        } catch (Exception e) {
//            String message = "新增退货QFF失败";
//            log.error(message,e);
//            throw new FebsException(message);
//        }
//    }
//
//    /**更新退货QFF
//     * @param refund
//     * @return
//     * @throws FebsException
//     */
//    @Qff("更新退货QFF")
//    @PostMapping("/edit")
//    @RequiresPermissions("refund:audit")
//    public FebsResponse editRefund(Refund refund) throws FebsException {
//        try {
//            Integer count = refundService.editRefund(refund);
//            return new FebsResponse().success();
//        } catch (Exception e) {
//            String message = "更新退货QFF失败";
//            log.error(message,e);
//            throw new FebsException(message);
//        }
//    }
//
//    /**查询退货QFF
//     * @param refund
//     * @return
//     */
//    @GetMapping("/list")
//    @RequiresPermissions("refund:view")
//    public FebsResponse getRefundPage(Refund refund) throws FebsException {
//        try {
//            IPage<Refund> pageInfo = refundService.getRefundPage(refund,getCurrentUser());
//            Map<String, Object> dataTable = getDataTable(pageInfo);
//            return new FebsResponse().success().data(dataTable);
//        } catch (Exception e) {
//            String message = "查询退货QFF失败";
//            log.error(message,e);
//            throw new FebsException(message);
//        }
//    }
//
//    /**删除退货QFF
//     * @param id
//     * @return
//     * @throws FebsException
//     */
//    @Qff("删除退货QFF")
//    @GetMapping("/deleteRefund/{id}")
//    @RequiresPermissions("refund:del")
//    public FebsResponse updateRefundStatus(@PathVariable Integer id) throws FebsException {
//        try {
//            Refund refund = new Refund();
//            refund.setId(id);
//            processService.deleteInstance(refund);
//            Integer count = refundService.updateRefundStatus(id, ProcessConstant.HAVE_ABNORMAL);
//            return new FebsResponse().success();
//        } catch (Exception e) {
//            String message = "删除退货QFF失败";
//            log.error(message,e);
//            throw new FebsException(message);
//        }
//    }
//
//    /**查询退货QFF
//     * @param id
//     * @return
//     * @throws FebsException
//     */
//    @GetMapping("/queryRefund")
//    @RequiresPermissions("refund:view")
//    public FebsResponse queryRefundById(Integer id) throws FebsException {
//        try {
//            Refund refund = refundService.queryRefundById(id);
//            return new FebsResponse().success().data(refund);
//        } catch (Exception e) {
//            String message = "查询退货QFF失败";
//            log.error(message,e);
//            throw new FebsException(message);
//        }
//    }
//
//    /**查询流程
//     * @param refund
//     * @return
//     */
//    @GetMapping("/queryHistory")
//    public FebsResponse queryHistory(Refund refund) throws FebsException {
//        try {
//            List<ProcessHistory> list = processService.queryHistory(refund);
//            return new FebsResponse().success().data(list);
//        } catch (Exception e) {
//            String message = "查询退货QFF流程失败";
//            log.error(message,e);
//            throw new FebsException(message);
//        }
//    }
//
//    /**提交流程
//     * @param refund
//     * @return
//     * @throws FebsException
//     */
//    @Qff("提交退货QFF流程")
//    @PostMapping("/commit")
//    @RequiresPermissions("refund:audit")
//    public FebsResponse commitProcess(Refund refund) throws FebsException {
//        try {
//            User user = getCurrentUser();
//            processService.commitProcess(refund,user);
//            return new FebsResponse().success();
//        } catch (Exception e) {
//            String message = "提交退货QFF流程失败";
//            log.error(message,e);
//            throw new FebsException(message);
//        }
//    }
//
//    /**同意当前任务
//     * @param refund
//     * @return
//     * @throws FebsException
//     */
//    @Qff("同意退货QFF任务")
//    @PostMapping("/agree")
//    @RequiresPermissions("refund:audit")
//    public FebsResponse agreeCurrentProcess(Refund refund) throws FebsException {
//        try {
//            User user = getCurrentUser();
//            List<String> group = processService.getGroupId(refund,user);
//            if(group.contains(user.getUsername())){
//                processService.agreeCurrentProcess(refund,user);
//            }else {
//                throw new FebsException("当前无权限或改数据已审核");
//            }
//            return new FebsResponse().success();
//        } catch (FebsException e) {
//            String message = "同意退货QFF流程失败";
//            log.error(message,e);
//            throw new FebsException(message);
//        }
//    }
//
//    /**导出excel
//     * @param refund
//     * @param response
//     */
//    @GetMapping("excel")
//    @RequiresPermissions("refund:down")
//    public void download(Refund refund, HttpServletResponse response) throws FebsException {
//        try {
//            IPage<Refund> refundPage = refundService.getRefundPage(refund,getCurrentUser());
//            List<Refund> refundList = refundPage.getRecords();
//            ExcelKit.$Export(Refund.class, response).downXlsx(refundList, false);
//        } catch (Exception e) {
//            String message = "导出退货QFFexcel失败";
//            log.error(message,e);
//            throw new FebsException(message);
//        }
//    }
//
//
//
////    /**查询用户当前任务
////     * @return
////     * @throws FebsException
////     */
////    @GetMapping("/check")
////    @RequiresPermissions("refund:audit")
////    public FebsResponse queryCurrentProcess() throws FebsException {
////        User user = getCurrentUser();
////        List<Refund> list = refundService.queryCurrentProcess(user);
////        if(CollectionUtils.isEmpty(list)){
////            throw new FebsException("没有任务");
////        }
////        return new FebsResponse().success().data(list);
////    }
//
//
//
//}
