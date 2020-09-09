package com.neefull.fsp.web.qff.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neefull.fsp.web.qff.entity.Attachment;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: chengchengchu
 * @Date: 2020/5/12  18:26
 */
@Component
public interface AttachmentMapper extends BaseMapper<Attachment> {


    void updateStatusById(@Param("id") Integer id,@Param("status") Integer status);

    List<Attachment> selectByNumberAndType(@Param("number") String number, @Param("type") String type);

    void updateStatusByNumberAndType(@Param("number") String number, @Param("typt") String typt);

    Attachment selectByNumberAndRemark(@Param("number") String number, @Param("attNumber") String attNumber);

    void deleteAttacheByNumberAndStage(@Param("number") String number, @Param("stage") String stage, @Param("url") String url);

}
