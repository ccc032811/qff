package com.neefull.fsp.web.qff.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neefull.fsp.web.qff.config.ProcessInstanceProperties;
import com.neefull.fsp.web.qff.entity.Attachment;
import com.neefull.fsp.web.qff.mapper.AttachmentMapper;
import com.neefull.fsp.web.qff.service.IAttachmentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

/**
 * @Author: chengchengchu
 * @Date: 2020/5/12  18:25
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class AttachmentServiceImpl  extends ServiceImpl<AttachmentMapper, Attachment> implements IAttachmentService {

    @Autowired
    private AttachmentMapper attachmentMapper;
    @Autowired
    private ProcessInstanceProperties properties;

    @Override
    public List<Attachment> selectAttsByQffId(String number,String type) {
        QueryWrapper<Attachment> wrapper = new QueryWrapper<>();
        wrapper.eq("qff_id",number);
        wrapper.eq("qff_type",type);
        return attachmentMapper.selectList(wrapper);
    }

    @Override
    public void updateStatusById(Integer id, Integer status) {
        attachmentMapper.updateStatusById(id,status);
    }

    @Override
    public void addAttachment(Attachment attachment) {
        attachmentMapper.insert(attachment);
    }

    @Override
    public void updateAttachment(Attachment attachment) {
        attachmentMapper.updateById(attachment);
    }

    @Override
    @Transactional
    public Boolean deleteImage(String url) {
        boolean delete = false;
        if(StringUtils.isNotEmpty(url)){
            File file = new File(properties.getImagePath()+url);
            if(file.exists()){
                 delete = file.delete();
            }
        }
        return delete;
    }

    @Override
    @Transactional
    public void deleteByNumber(String number,String typt) {
        QueryWrapper<Attachment> delete = new QueryWrapper<>();
        delete.eq("qff_id",number);
        delete.eq("qff_type",typt);
        attachmentMapper.delete(delete);
    }

    @Override
    public Boolean selectAttAndNumber(String number, String attNumber) {
        QueryWrapper<Attachment> wrapper = new QueryWrapper<>();
        wrapper.eq("qff_id",number);
        wrapper.eq("remark",attNumber);
        Attachment attachment = attachmentMapper.selectOne(wrapper);
        if(attachment!=null){
            return true;
        }else {
            return false;
        }


    }
}
