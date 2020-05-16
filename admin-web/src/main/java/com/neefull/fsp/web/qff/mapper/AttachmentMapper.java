package com.neefull.fsp.web.qff.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neefull.fsp.web.qff.entity.Attachment;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

/**
 * @Author: chengchengchu
 * @Date: 2020/5/12  18:26
 */
@Component
public interface AttachmentMapper extends BaseMapper<Attachment> {


    void updateStatusById(@Param("id") Integer id,@Param("status") Integer status);
}
