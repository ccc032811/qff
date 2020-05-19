package com.neefull.fsp.web.qff.service;

import com.neefull.fsp.web.system.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @Author: chengchengchu
 * @Date: 2020/3/13  15:04
 */
public interface IFileService {


    /**上传图片
     * @param file
     * @return
     */
    String uploadImage(MultipartFile file,String number);


    /**解析excel
     * @param file
     */
    void resolverExcel(MultipartFile file, User user);
}
