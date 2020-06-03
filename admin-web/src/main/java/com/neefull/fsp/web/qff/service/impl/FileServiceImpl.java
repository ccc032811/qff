package com.neefull.fsp.web.qff.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.neefull.fsp.web.common.exception.FebsException;
import com.neefull.fsp.web.qff.config.ProcessInstanceProperties;
import com.neefull.fsp.web.qff.config.SendMailProperties;
import com.neefull.fsp.web.qff.entity.Attachment;
import com.neefull.fsp.web.qff.entity.Recent;
import com.neefull.fsp.web.qff.entity.RecentResolver;
import com.neefull.fsp.web.qff.service.IAttachmentService;
import com.neefull.fsp.web.qff.service.IFileService;
import com.neefull.fsp.web.qff.service.IProcessService;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.system.entity.User;
import com.neefull.fsp.web.system.service.IUserService;
import com.sun.mail.util.MailSSLSocketFactory;
import com.wuwenze.poi.ExcelKit;
import com.wuwenze.poi.handler.ExcelReadHandler;
import com.wuwenze.poi.pojo.ExcelErrorField;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * @Author: chengchengchu
 * @Date: 2020/3/13  15:04
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class FileServiceImpl implements IFileService {

    private static final Integer SELECT_NUMBER =2;


    @Autowired
    private IProcessService processService;
    @Autowired
    private ProcessInstanceProperties properties;
    @Autowired
    private SendMailProperties mailProperties;
    @Autowired
    private IUserService userService;
    @Autowired
    private TemplateEngine templateEngine;


    @Override
    @Transactional
    public String uploadImage(MultipartFile file,String number) {

        String originalFilename = file.getOriginalFilename();
        String filename = number+"-"+originalFilename;
        File filePath = new File(properties.getImagePath(), filename);

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
        return properties.getImageUrl()+filename;

    }

    @Override
    @Transactional
    public void resolverExcel(MultipartFile file, User user) {

        List<Recent> list = new ArrayList<>();
        List<Integer> errorList = new ArrayList();

        if (file.isEmpty()) {
            log.error("当前文件为空");
        }
        try {
            ExcelKit.$Import(RecentResolver.class).readXlsx(file.getInputStream(), new ExcelReadHandler<RecentResolver>() {
                @Override
                public void onSuccess(int sheetIndex, int rowIndex, RecentResolver entity) {
                    if(rowIndex > SELECT_NUMBER){
                        Recent recent = new Recent();
                        if(entity!=null){
                            if(entity.getkMater().equals("康德乐物料号")||entity.getrMater().equals("罗氏物料号")) {
                                return;
                            }else {
                                if(entity.getkMater().equals("$EMPTY_CELL$")&&entity.getrMater().equals("$EMPTY_CELL$")&&entity.getName().equals("$EMPTY_CELL$")) {
                                    return;
                                }else {
                                    if(!entity.getkMater().equals("$EMPTY_CELL$")){
                                        recent.setkMater(entity.getkMater());
                                    }else {
                                        recent.setkMater("");
                                    }
                                    if(!entity.getrMater().equals("$EMPTY_CELL$")){
                                        recent.setrMater(entity.getrMater());
                                    }else {
                                        recent.setrMater("");
                                    }
                                    if(!entity.getName().equals("$EMPTY_CELL$")){
                                        recent.setName(entity.getName());
                                    }else {
                                        recent.setName("");
                                    }
                                    if(!entity.getUseLife().equals("$EMPTY_CELL$")){
                                        recent.setUseLife(entity.getUseLife());
                                    }else {
                                        recent.setUseLife("");
                                    }
                                    if(!entity.getBatch().equals("$EMPTY_CELL$")){
                                        recent.setBatch(entity.getBatch());
                                    }else {
                                        recent.setBatch("");
                                    }
                                    if(!entity.getSapBatch().equals("$EMPTY_CELL$")){
                                        recent.setSapBatch(entity.getSapBatch());
                                    }else {
                                        recent.setSapBatch("");
                                    }
                                    if(!entity.getFactory().equals("$EMPTY_CELL$")){
                                        recent.setFactory(entity.getFactory());
                                    }else {
                                        recent.setFactory("");
                                    }
                                    if(!entity.getWareHouse().equals("$EMPTY_CELL$")){
                                        recent.setWareHouse(entity.getWareHouse());
                                    }else {
                                        recent.setWareHouse("");
                                    }
                                    if(!entity.getNumber().equals("$EMPTY_CELL$")){
                                        recent.setNumber(entity.getNumber());
                                    }else {
                                        recent.setNumber("");
                                    }
                                }
                            }

                        }
                        list.add(recent);
                    }
                }
                @Override
                public void onError(int sheetIndex, int rowIndex, List<ExcelErrorField> errorFields) {
                    errorList.add(rowIndex+1);
                }
            });
        } catch (IOException e) {
            log.error("导入文件失败,原因为：{}",e.getMessage());
        }

        if(CollectionUtils.isNotEmpty(list)){
            for (Recent recent : list) {
                processService.commitProcess(recent,user);
            }
        }

        if(CollectionUtils.isNotEmpty(errorList)){
            String count = "";
            for (int i=0;i<=errorList.size()-1;i++) {
                if(i==errorList.size()-1){
                    count+=errorList.get(i)+"行";
                }
                count+=errorList.get(i) +"行 ,";
            }
            log.info("导入失败数据,失败行数"+count);
        }
        //发送邮件
        Context context = new Context();
        context.setVariable("list",list);
        String text = templateEngine.process("rocheRecent", context);

        String[] emails = getEmails(86);
        sendMail(text,emails);
    }


    @Transactional
    public  void sendMail(String text,String[] emails) {

        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(mailProperties.getHost());
        javaMailSender.setDefaultEncoding(mailProperties.getCharset());
        javaMailSender.setProtocol(mailProperties.getProtocol());
        javaMailSender.setPort(Integer.parseInt(mailProperties.getPort()));
        javaMailSender.setUsername(mailProperties.getUsername());//发送者的邮箱
        javaMailSender.setPassword(mailProperties.getPassword());//发送者的密码

        Properties prop = new Properties();
        prop.setProperty("mail.smtp.auth", mailProperties.getAuth());
//        prop.setProperty("mail.smtp.timeout", mailProperties.getTimeout());
//        try {
//            MailSSLSocketFactory sf = new MailSSLSocketFactory();
//            sf.setTrustAllHosts(true);
//            prop.put("mail.smtp.ssl.enable", true);
//            prop.put("mail.smtp.ssl.socketFactory", sf);
//        } catch (
//                GeneralSecurityException e) {
//            e.printStackTrace();
//        }
        javaMailSender.setJavaMailProperties(prop);

        MimeMessageHelper mimeMessageHelper = null;
        try {
            mimeMessageHelper = new MimeMessageHelper(javaMailSender.createMimeMessage(), true);
            mimeMessageHelper.setFrom(mailProperties.getUsername());//发送的邮箱地址
            mimeMessageHelper.setTo(emails);//接收的邮箱地址
//            mimeMessageHelper.setTo("wangpei_it@163.com");//接收的邮箱地址
//            mimeMessageHelper.setCc("");//抄送者的邮箱地址
            mimeMessageHelper.setSubject("您当前需要处理的文件");//邮件名称
            mimeMessageHelper.setText(text,true);//邮箱文字内容

        } catch (
                MessagingException e) {
            e.printStackTrace();
        }

        javaMailSender.send(mimeMessageHelper.getMimeMessage());
    }


    public String[] getEmails(Integer id){
        List<User> userList = userService.findUserByRoleId(id);
        List<String> userMails = new ArrayList<>();
        for (User user : userList) {
            userMails.add(user.getEmail());
        }
        return userMails.toArray(new String[0]);
    }
}
