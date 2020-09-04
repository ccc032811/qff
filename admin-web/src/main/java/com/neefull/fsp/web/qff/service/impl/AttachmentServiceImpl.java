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
        return attachmentMapper.selectByNumberAndType(number,type);
    }

    @Override
    @Transactional
    public void updateStatusById(Integer id, Integer status) {
        attachmentMapper.updateStatusById(id,status);
    }

    @Override
    @Transactional
    public void addAttachment(Attachment attachment) {
        attachmentMapper.insert(attachment);
    }

    @Override
    @Transactional
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
    public Boolean removeImage(String number, String url, String stage) {

        attachmentMapper.deleteAttacheByNumberAndStage(number,stage);
        return deleteImage(url);

    }

    @Override
    @Transactional
    public void deleteByNumber(String number,String typt) {

        attachmentMapper.updateStatusByNumberAndType(number,typt);

    }

    @Override
    public Boolean selectAttAndNumber(String number, String attNumber) {
        Attachment attachment =  attachmentMapper.selectByNumberAndRemark(number,attNumber);
        if(attachment!=null){
            return true;
        }else {
            return false;
        }


    }


}
