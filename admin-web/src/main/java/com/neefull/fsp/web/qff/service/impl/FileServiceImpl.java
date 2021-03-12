package com.neefull.fsp.web.qff.service.impl;


import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.neefull.fsp.web.qff.config.ProcessInstanceProperties;
import com.neefull.fsp.web.qff.config.SendMailProperties;
import com.neefull.fsp.web.qff.service.IFileService;
import com.neefull.fsp.web.qff.service.IProcessService;
import com.neefull.fsp.web.system.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;


import java.io.File;
import java.io.IOException;
import java.util.Date;


/**
 * @Author: chengchengchu
 * @Date: 2020/3/13  15:04
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class FileServiceImpl implements IFileService {


    @Autowired
    private ProcessInstanceProperties properties;


    @Override
    @Transactional
    public String uploadImage(MultipartFile file,String number) {
        //上传文件
        String originalFilename = file.getOriginalFilename().replace(String.valueOf((char)160),"_").replaceAll("\\s+","_");

        int unixp = originalFilename.lastIndexOf("/");
        int winp = originalFilename.lastIndexOf("\\");
        int posp = (winp > unixp ? winp : unixp);
        if (posp != -1) {
            originalFilename = originalFilename.substring(posp + 1);
        }
        String fileName = DateFormatUtils.format(new Date(),"yyyyMMddHHmmss")+"_"+originalFilename;

        File filePath = new File(properties.getImagePath(), fileName);
        String[] paths = properties.getImagePath().split(StringPool.SLASH);
        String dir = paths[0];
        for (int i = 0; i < paths.length - 1; i++) {
            try {
                dir = dir + "/" + paths[i + 1];
                File dirFile = new File(dir);
                if (!dirFile.exists()) {
                    dirFile.mkdir();
                    System.out.println("创建目录为：" + dir);
                }
            } catch (Exception err) {
                System.err.println("文件夹创建发生异常");
            }
        }

        try {
            file.transferTo(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getImageUrl()+fileName;

    }



}
