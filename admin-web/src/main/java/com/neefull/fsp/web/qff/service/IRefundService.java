//package com.neefull.fsp.web.qff.service;
//
//import com.baomidou.mybatisplus.core.metadata.IPage;
//import com.baomidou.mybatisplus.extension.service.IService;
//import com.neefull.fsp.web.qff.entity.Refund;
//import com.neefull.fsp.web.system.entity.User;
//
//
///**退货QFF
// * @Author: chengchengchu
// * @Date: 2019/11/29  13:53
// */
//
//
//public interface IRefundService extends IService<Refund> {
//
//
//    /**新增退货QFF
//     * @param refund
//     * @return
//     */
//    Integer addRefund(Refund refund);
//
//    /**更新退货QFF
//     * @param refund
//     * @return
//     */
//    Integer editRefund(Refund refund);
//
//    /**查询退货QFF
//     * @param refund
//     * @return
//     */
//    IPage<Refund> getRefundPage(Refund refund, User user);
//
//    /**删除退货QFF
//     * @param
//     * @return
//     */
//    Integer updateRefundStatus(Integer id,Integer status);
//
//    /**查询退货QFF
//     * @param
//     * @return
//     */
//    Refund queryRefundById(Integer id);
//
//    /**根据ＱＦＦ编号查询
//     * @param number
//     * @return
//     */
//    Refund queryRefundByNumber(String number);
//}
