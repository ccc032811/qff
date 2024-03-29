package com.neefull.fsp.web.qff.utils;

import com.neefull.fsp.web.qff.config.SendMailProperties;
import com.neefull.fsp.web.qff.config.SpringBeanUtil;
import com.neefull.fsp.web.system.entity.User;
import com.neefull.fsp.web.system.service.IUserService;
import com.sun.mail.util.MailSSLSocketFactory;

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


    public static String[] getEmails(Integer id){
        IUserService userService = SpringBeanUtil.getObject(IUserService.class);
        List<User> userList = userService.findUserByRoleId(id);
        List<String> userMails = new ArrayList<>();
        for (User user : userList) {
            if(StringUtils.isNotEmpty(user.getEmail())
                    &&user.getAccept().equals(String.valueOf(ProcessConstant.NEW_BUILD))
                    &&user.getStatus().equals(String.valueOf(ProcessConstant.NEW_BUILD))) {
                userMails.add(user.getEmail());
            }
        }
        return userMails.toArray(new String[0]);
    }

    public static void sendMail(String type,String text, SendMailProperties mailProperties, String[] mails, Map<String,String> files ) {

        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(mailProperties.getHost());
        javaMailSender.setDefaultEncoding(mailProperties.getCharset());
        javaMailSender.setProtocol(mailProperties.getProtocol());
        javaMailSender.setPort(Integer.parseInt(mailProperties.getPort()));
        javaMailSender.setUsername(mailProperties.getUsername());
        javaMailSender.setPassword(mailProperties.getPassword());

        Properties prop = new Properties();
        prop.setProperty("mail.smtp.auth", mailProperties.getAuth());
        prop.setProperty("mail.smtp.timeout", mailProperties.getTimeout());
        //开启SSL连接
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
            if(mails.length!=0){
                mimeMessageHelper = new MimeMessageHelper(javaMailSender.createMimeMessage(), true);
                mimeMessageHelper.setFrom(mailProperties.getUsername());
                mimeMessageHelper.setTo(mails);
                if (StringUtils.isEmpty(type)) {
                    mimeMessageHelper.setSubject("您当前有需要处理的类型");
                } else{
                    mimeMessageHelper.setSubject("您当前有需要处理的" + type + "类型");
                }
                mimeMessageHelper.setText(text,true);
                if(!files.isEmpty()){
                    Set<String> strings = files.keySet();
                    for (String string : strings) {
                        FileSystemResource resource = new FileSystemResource(new File(files.get(string)));
                        mimeMessageHelper.addAttachment(string , resource);
                    }
                }
                javaMailSender.send(mimeMessageHelper.getMimeMessage());
            }

        } catch (
                MessagingException e) {
            e.printStackTrace();
        }


    }



}




