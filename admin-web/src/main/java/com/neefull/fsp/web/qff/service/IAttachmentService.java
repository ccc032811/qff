package com.neefull.fsp.web.qff.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neefull.fsp.web.qff.entity.Attachment;

import java.util.List;

/**
 * @Author: chengchengchu
 * @Date: 2020/5/12  18:24
 */
public interface IAttachmentService extends IService<Attachment> {

    /**根据numberid  查询
     * @param number
     * @return
     */
    List<Attachment> selectAttsByQffId(String number,String type);

    /**新增
     * @param attachment
     */
    void addAttachment(Attachment attachment);

    /**更新数据
     * @param attachment
     */
    void updateAttachment(Attachment attachment);

    /**删除图片
     * @param url
     */
    Boolean deleteImage(String url);

    void deleteByNumber(String number,String typt);

    Boolean selectAttAndNumber(String number, String attNumber);

    Boolean removeImage(String number, String url, String stage);

}
