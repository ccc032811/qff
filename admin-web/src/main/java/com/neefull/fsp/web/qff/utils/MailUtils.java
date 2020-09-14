package com.neefull.fsp.web.qff.utils;

import com.neefull.fsp.web.qff.config.SendMailProperties;
import com.neefull.fsp.web.system.entity.User;
import com.neefull.fsp.web.system.service.IUserService;
import com.sun.mail.util.MailSSLSocketFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.File;
import java.io.FileInputStream;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * @Author: chengchengchu
 * @Date: 2020/1/8  15:01
 */

public class MailUtils {

    public static void sendMail(String text, SendMailProperties mailProperties, String[] mails, Map<String,String> files ) {

        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(mailProperties.getHost());
        javaMailSender.setDefaultEncoding(mailProperties.getCharset());
        javaMailSender.setProtocol(mailProperties.getProtocol());
        javaMailSender.setPort(Integer.parseInt(mailProperties.getPort()));
        javaMailSender.setUsername(mailProperties.getUsername());//发送者的邮箱
        javaMailSender.setPassword(mailProperties.getPassword());//发送者的密码

        Properties prop = new Properties();
        prop.setProperty("mail.smtp.auth", mailProperties.getAuth());
        prop.setProperty("mail.smtp.timeout", mailProperties.getTimeout());
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
            mimeMessageHelper.setTo(mails);//接收的邮箱地址
//            mimeMessageHelper.setTo("wangpei_it@163.com");//接收的邮箱地址
//            mimeMessageHelper.setCc("");//抄送者的邮箱地址
            mimeMessageHelper.setSubject("您当前需要处理的文件");//邮件名称
            mimeMessageHelper.setText(text,true);//邮箱文字内容
            if(!files.isEmpty()){
                Set<String> strings = files.keySet();
                for (String string : strings) {
                    FileSystemResource resource = new FileSystemResource(new File(files.get(string)));
                    mimeMessageHelper.addAttachment(string , resource);
                }
            }
        } catch (
                MessagingException e) {
            e.printStackTrace();
        }
        javaMailSender.send(mimeMessageHelper.getMimeMessage());
    }

}




