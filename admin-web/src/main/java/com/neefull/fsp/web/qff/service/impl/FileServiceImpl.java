package com.neefull.fsp.web.qff.service.impl;

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
import com.neefull.fsp.web.system.entity.User;
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


    @Override
    @Transactional
    public Map<String, String> uploadImage(MultipartFile file,String number) {

        String originalFilename = file.getOriginalFilename();
//        String extension = StringUtils.substringAfterLast(filename, StringPool.DOT);
        String sf = UUID.randomUUID().toString();
        String filename = number+"-"+originalFilename;

        File filePath = new File(properties.getImagePath(), filename);

        Map<String,String> map =new HashMap<>();
        try {

            file.transferTo(filePath);
            map.put("index",sf.replaceAll(StringPool.DASH,StringPool.EMPTY));
            map.put("value",filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;


//        try {
//            BufferedImage image = ImageIO.read(file.getInputStream());
//            if (image == null) {
//                throw new RuntimeException();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        //创建文件存储路径
//        String[] paths = properties.getImagePath().split(StringPool.SLASH);
//        String dir = paths[0];
//        for (int i = 0; i < paths.length - 1; i++) {
//            try {
//                dir = dir + "/" + paths[i + 1];
//                File dirFile = new File(dir);
//                if (!dirFile.exists()) {
//                    dirFile.mkdir();
//                    System.out.println("创建目录为：" + dir);
//                }
//            } catch (Exception err) {
//                System.err.println("文件夹创建发生异常");
//            }
//        }

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
                        recent.setTransport(entity.getTransport());
                        recent.setkMater(entity.getkMater());
                        recent.setrMater(entity.getrMater());
                        recent.setName(entity.getName());
                        recent.setUseLife(entity.getUseLife());
                        recent.setBatch(entity.getBatch());
                        recent.setSapBatch(entity.getSapBatch());
                        recent.setFactory(entity.getFactory());
                        recent.setWareHouse(entity.getWareHouse());
                        recent.setNumber(entity.getNumber());
                        recent.setrConf(entity.getrConf());
                        list.add(recent);
//                        recentService.addRecent(recent);
                        processService.commitProcess(recent,user);
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

        if(errorList.size()>0){
            String count = null;
            for (int i=0;i<=errorList.size()-1;i++) {
                if(i==errorList.size()-1){
                    count+=errorList.get(i)+"行";
                }
                count += errorList.get(i) +"行 ,";
            }
            log.info("导入失败数据,失败行数"+count);
        }
        //发送邮件

        StringBuilder content=new StringBuilder("<html><head></head><body><h3>你好</h3>");
        content.append("<tr><h3>具体详情如下表所示:</h3></tr>");
        content.append("<table border='5' style='border:solid 1px #000;font-size=10px;'>");
        content.append("<tr style='background-color: #00A1DD'><td>康德乐物料号</td>" +
                "<td>罗氏物料号</td><td>产品物料号</td><td>有效期</td>" +
                "<td>批号</td><td>SAP批次</td><td>工厂</td><td>库位</td><td>数量</td></tr>");
        for (Recent recent : list) {
            content.append("<tr><td>"+recent.getkMater()+"</td><td>"+recent.getrMater()+"</td>" +
                    "<td>"+recent.getName()+"</td><td>"+recent.getUseLife()+"</td>" +
                    "<td>"+recent.getBatch()+"</td><td>"+recent.getSapBatch()+"</td>" +
                    "<td>"+recent.getFactory()+"</td><td>"+recent.getWareHouse()+"</td><td>"+recent.getNumber()+"</td></tr>");
        }
        content.append("</table>");
        content.append("</body></html>");

        String text = content.toString();
        sendMail(text);
    }


    @Transactional
    public  void sendMail(String text) {

        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(mailProperties.getHost());
        javaMailSender.setDefaultEncoding(mailProperties.getCharset());
        javaMailSender.setProtocol(mailProperties.getProtocol());
        javaMailSender.setPort(Integer.parseInt(mailProperties.getPort()));
        javaMailSender.setUsername("ccc032811@163.com");//发送者的邮箱
        javaMailSender.setPassword("ccc032811");//发送者的密码

        Properties prop = new Properties();
        prop.setProperty("mail.smtp.auth", mailProperties.getAuth());
//        prop.setProperty("mail.smtp.timeout", mailProperties.getTimeout());
        try {
            MailSSLSocketFactory sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
            prop.put("mail.smtp.ssl.enable", true);
            prop.put("mail.smtp.ssl.socketFactory", sf);
        } catch (
                GeneralSecurityException e) {
            e.printStackTrace();
        }
        javaMailSender.setJavaMailProperties(prop);

        MimeMessageHelper mimeMessageHelper = null;
        try {
            mimeMessageHelper = new MimeMessageHelper(javaMailSender.createMimeMessage(), true);
            mimeMessageHelper.setFrom("ccc032811@163.com");//发送的邮箱地址
            mimeMessageHelper.setTo("ccc032811@163.com");//接收的邮箱地址
//            mimeMessageHelper.setTo("wangpei_it@163.com");//接收的邮箱地址
//            mimeMessageHelper.setCc("");//抄送者的邮箱地址
            mimeMessageHelper.setSubject("测试Springboot发送带附件的邮件,用来测试的");//邮件名称
            mimeMessageHelper.setText(text,true);//邮箱文字内容

        } catch (
                MessagingException e) {
            e.printStackTrace();
        }

        javaMailSender.send(mimeMessageHelper.getMimeMessage());
    }
}
