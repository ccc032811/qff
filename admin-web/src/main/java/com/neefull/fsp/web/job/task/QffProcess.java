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
import org.apache.commons.lang3.StringUtils;
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
                processService.startProcess(commodity);
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
                if(StringUtils.isNotEmpty(user.getEmail())&&user.getAccept().equals("1")&&user.getStatus().equals("1")) {
                    userMails.add(user.getEmail());
                }
            }
            String[] mails = userMails.toArray(new String[0]);


            MailUtils.sendMail(text, mailProperties, mails, files);
        }

    }


}
