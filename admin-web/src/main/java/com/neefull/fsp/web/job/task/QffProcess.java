package com.neefull.fsp.web.job.task;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.qff.config.ProcessInstanceProperties;
import com.neefull.fsp.web.qff.config.SendMailProperties;
import com.neefull.fsp.web.qff.config.SftpProperties;
import com.neefull.fsp.web.qff.entity.Attachment;
import com.neefull.fsp.web.qff.entity.Commodity;
import com.neefull.fsp.web.qff.service.IAttachmentService;
import com.neefull.fsp.web.qff.service.ICommodityService;
import com.neefull.fsp.web.qff.service.IProcessService;
import com.neefull.fsp.web.qff.utils.MailUtils;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.qff.utils.SftpUtils;
import com.neefull.fsp.web.system.entity.User;
import com.neefull.fsp.web.system.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.tomcat.jni.Proc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.util.*;

/**
 * @Author: chengchengchu
 * @Date: 2020/5/12  18:32
 */
@Slf4j
@Component
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class QffProcess extends BaseController {

    @Autowired
    private ICommodityService commodityService;
    @Autowired
    private IAttachmentService attachmentService;
    @Autowired
    private IProcessService processService;
    @Autowired
    private SendMailProperties mailProperties;
    @Autowired
    private IUserService userService;
    @Autowired
    private SftpProperties properties;
    @Autowired
    private TemplateEngine templateEngine;


    @Transactional
    public void getAttmentAndStart() {

        List<Commodity> commodityEmail = new ArrayList<>();
        Map<String, String> files = new HashMap<>();

        List<Commodity> commodityList = commodityService.selectAllCommodity();
        if (CollectionUtils.isNotEmpty(commodityList)) {
            for (Commodity commodity : commodityList) {
                if (commodity.getAccessory() == 0) {
                    commodityEmail.add(commodity);
                } else if (commodity.getAccessory() != 0 ) {
                    int count =0;
                    List<Attachment> attachments = attachmentService.selectAttsByQffId(commodity.getNumber(), commodity.getStage());
                    if (CollectionUtils.isNotEmpty(attachments)) {
                        SftpUtils sftp = null;
                        try {
                            sftp = new SftpUtils(properties.getHost(), properties.getUsername(), properties.getPassword());
                            sftp.connect();

                            for (Attachment attachment : attachments) {
                                boolean isDown = sftp.downloadFile(properties.getSftpPath(), attachment.getRemark() + StringPool.DOT + attachment.getAttachType(), properties.getLocalPath(), attachment.getRemark() + StringPool.DOT + attachment.getAttachType());
                                if (isDown) {
                                    count = count += 1;
                                    File file = new File(properties.getLocalPath() + attachment.getRemark() + StringPool.DOT + attachment.getAttachType());
                                    if (file.exists() && file.isFile()) {
                                        attachment.setEnable(1);
                                        attachment.setAttachSize(file.length());
                                        attachmentService.updateAttachment(attachment);
                                    }
                                }
                            }
                            if (count == commodity.getAccessory()) {
                                commodityEmail.add(commodity);
                                for (Attachment attachment : attachments) {
                                    String newName = attachment.getRemark() + DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss") + StringPool.DOT + attachment.getAttachType();
                                    sftp.remove(properties.getSftpPath() + attachment.getRemark() + StringPool.DOT + attachment.getAttachType(), properties.getMovepath() + newName);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            sftp.disconnect();
                        }

                    }
                }
            }
        }


        if (CollectionUtils.isNotEmpty(commodityEmail)) {
            for (Commodity commodity : commodityEmail) {
                processService.commitProcess(commodity, new User());
                commodityService.updateCommodityStatus(commodity.getId(), ProcessConstant.UNDER_REVIEW);
            }
        }



        if (CollectionUtils.isNotEmpty(commodityEmail)) {
            // 发送邮件
            Context context = new Context();
            context.setVariable("list", commodityEmail);
            String text = templateEngine.process("rocheCommodity", context);

            //查询收件人
            List<User> userList = userService.findUserByRoleId(86);
            List<String> userMails = new ArrayList<>();
            for (User user : userList) {
                userMails.add(user.getEmail());
            }
            String[] mails = userMails.toArray(new String[0]);

            //发送邮件
            MailUtils.sendMail(text, mailProperties, mails, files);
        }



    }




//        //下载文件获取所有的文件名
//        SftpUtils sftp = null;
//        List<String> list = new ArrayList<>();
//        try {
//            sftp = new SftpUtils(properties.getHost(),properties.getUsername(),properties.getPassword());
//            sftp.connect();
//            list = sftp.batchDownLoadFile(properties.getSftpPath(), properties.getLocalPath());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }finally {
//            sftp.disconnect();
//        }
//
//        Map<String,Commodity> commoditys = new HashMap<>();
//        Map<String,String> files = new HashMap<>();
//
//        if(CollectionUtils.isNotEmpty(list)){
//            Iterator<String> iterator = list.iterator();
//            while (iterator.hasNext()){
//                String fileName = iterator.next();
//                String number = fileName.split("_")[0];
//                String attNumber = fileName.split("\\.")[0];
//                //根据number 去查询
//                Commodity commodity = commodityService.queryCommodityByNumber(number);
//                //判断该条数据是否存在
//                if(commodity!=null){
//                    //存在判断存在
//                    if(commodity.getStatus()==1) {
//                        //状态为1，添加到要发送邮件的集合中去
//                        commoditys.put(number, commodity);
//                        //插入新的关联附件数据
//                        Attachment attachment = new Attachment();
//                        attachment.setQffId(number);
//                        attachment.setQffType(commodity.getStage());
//                        attachment.setAttachType(fileName.substring(fileName.lastIndexOf(".") + 1));
//                        attachment.setAttachSize(new File(properties.getLocalPath() + fileName).length() / 1024);
//                        attachment.setRemark(fileName.substring(0, fileName.lastIndexOf(".")));
//                        attachment.setSource(1);
//                        attachment.setEnable(1);
//                        attachmentService.addAttachment(attachment);
//                        //加入要发送的附件集合
////                        files.put(fileName, properties.getLocalPath() + fileName);
//                    }else {
//                        //状态不为1的情况下，判断这天数据是否存在
//                        Boolean isAtt = attachmentService.selectAttAndNumber(number, attNumber);
//                        if (!isAtt) {
//                            //不存在的情况下添加该条数据
//                            Attachment attachment = new Attachment();
//                            attachment.setQffId(number);
//                            attachment.setQffType(commodity.getStage());
//                            attachment.setAttachType(fileName.substring(fileName.lastIndexOf(".") + 1));
//                            attachment.setAttachSize(new File(properties.getLocalPath() + fileName).length() / 1024);
//                            attachment.setRemark(fileName.substring(0, fileName.lastIndexOf(".")));
//                            attachment.setSource(1);
//                            attachment.setEnable(1);
//                            attachmentService.addAttachment(attachment);
//                        }
//                    }
//                }else {
//                    //不存在删除这条数据
//                    iterator.remove();
//                }
//            }
//        }
//
//        List<Commodity> commodityList = new ArrayList<>();
//
//        if(!commoditys.isEmpty()){
//            Set<String> strings = commoditys.keySet();
//            for (String string : strings) {
//                Commodity commodity = commoditys.get(string);
//                processService.commitProcess(commodity,new User());
//                commodityService.updateCommodityStatus(commodity.getId(), ProcessConstant.UNDER_REVIEW);
//                commodityList.add(commodity);
//            }
//
//            // 发送邮件
//            Context context = new Context();
//            context.setVariable("list",commodityList);
//            String text = templateEngine.process("rocheCommodity", context);
//
//            //查询收件人
//            List<User> userList = userService.findUserByRoleId(86);
//            List<String> userMails = new ArrayList<>();
//            for (User user : userList) {
//                userMails.add(user.getEmail());
//            }
//            String[] mails = userMails.toArray(new String[0]);
//
//            //发送邮件
//            MailUtils.sendMail(text,mailProperties,mails,files);
//        }
//
//        try {
//            sftp = new SftpUtils(properties.getHost(),properties.getUsername(),properties.getPassword());
//            sftp.connect();
//
//            //移动文件
//            if(CollectionUtils.isNotEmpty(list)){
//                for (String string : list) {
//                    String name = string.substring(0, string.lastIndexOf("."));
//                    String type = string.substring(string.lastIndexOf(".") + 1);
//
//                    String newName = name+ DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss")+"."+type;
//                    sftp.remove(properties.getSftpPath()+string,properties.getMovepath()+newName);
////                        sftp.deleteSFTP(properties.getSftpPath(),string);
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }finally {
//            sftp.disconnect();
//        }



}
