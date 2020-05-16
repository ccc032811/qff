//package com.neefull.fsp.web.qff.service;
//
//import com.baomidou.mybatisplus.extension.service.IService;
//import com.neefull.fsp.web.qff.entity.DateImage;
//
//import java.util.List;
//
///**
// * @Author: chengchengchu
// * @Date: 2019/12/26  18:33
// */
//public interface IDateImageService extends IService<DateImage> {
//
//    /**根据id和类型查询
//     * @param dataId
//     * @param vest
//     */
//    DateImage queryImage(Integer dataId, String vest,String relevance);
//
//
//    /**查询图片
//     * @param dataId
//     * @param relevance
//     * @return
//     */
//    List<String> findImageByIdAndForm(Integer dataId, String relevance);
//
//    /**插入图片信息
//     * @param dataId
//     * @param vest
//     * @param relevance
//     * @param url
//     */
//    void insertDateImage(Integer dataId, String vest, String relevance, String url);
//
//    /**更新图片信息
//     * @param dataId
//     * @param vest
//     * @param relevance
//     * @param image
//     */
//    void updateDateImage(Integer dataId, String vest, String relevance, String image);
//
//    /**删除图片
//     * @param url
//     */
//    void deleteImage(String url);
//
//}
