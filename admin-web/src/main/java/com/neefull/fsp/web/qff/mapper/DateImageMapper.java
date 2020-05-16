//package com.neefull.fsp.web.qff.mapper;
//
//import com.baomidou.mybatisplus.core.mapper.BaseMapper;
//import com.neefull.fsp.web.qff.entity.DateImage;
//import org.apache.ibatis.annotations.Param;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
///**
// * @Author: chengchengchu
// * @Date: 2019/12/26  18:32
// */
//@Component
//public interface DateImageMapper extends BaseMapper<DateImage> {
//
//    /**根据id和类型查询
//     * @param dataId
//     * @param vest
//     * @return
//     */
//    DateImage queryImage(@Param("dataId") Integer dataId, @Param("vest") String vest, @Param("relevance") String relevance);
//
//    /**
//     * @param dataId
//     * @param relevance
//     * @return
//     */
//    List<DateImage> findImageByIdAndForm(@Param("dataId") Integer dataId, @Param("relevance") String relevance);
//}
