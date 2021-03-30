package com.neefull.fsp.web.qff.listener;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.qff.config.ProcessInstanceProperties;
import com.neefull.fsp.web.qff.config.SendMailProperties;
import com.neefull.fsp.web.qff.config.TemplateProperties;
import com.neefull.fsp.web.qff.entity.Attachment;
import com.neefull.fsp.web.qff.entity.Commodity;
import com.neefull.fsp.web.qff.service.IAttachmentService;
import com.neefull.fsp.web.qff.service.ICommodityService;
import com.neefull.fsp.web.qff.utils.FilePdfTemplate;
import com.neefull.fsp.web.qff.utils.MailUtils;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.system.entity.User;
import com.neefull.fsp.web.system.service.IUserService;
import com.sun.mail.util.MailSSLSocketFactory;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import java.io.File;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * 到货养护包装QFF邮件发送监听器
 *
 * @Author: chengchengchu
 * @Date: 2019/11/28  14:25
 */
@Slf4j
@Service("commodityListener")
public class CommodityListener extends BaseController implements JavaDelegate{

    @Autowired
    private SendMailProperties mailProperties;
    @Autowired
    private ICommodityService conserveService;
    @Autowired
    private IUserService userService;
    @Autowired
    private ProcessInstanceProperties properties;
    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public void execute(DelegateExecution execution) {

        String[] mails = null;

        if(execution.getCurrentActivityName().equals("罗氏")){
            //罗氏的邮箱
            mails = getEmails(86);
        }else if(execution.getCurrentActivityName().equals("康德乐")){
            //康德的邮箱
            mails = getEmails(87);
        }

        //获取流程的id
        String businessKey = execution.getProcessBusinessKey();
        String starId = "";
        if (businessKey.startsWith(Commodity.class.getSimpleName())) {
            if (StringUtils.isNotBlank(businessKey)) {
                //截取字符串
                starId = businessKey.split("\\:")[1].toString();
            }
        }
        //查询
        Commodity commodity = conserveService.queryCommodityById(Integer.parseInt(starId));

        Map<String,String> files = new HashMap<>();

        if(commodity.getStage().equals(ProcessConstant.WRAPPER_NAME)){

            List<Attachment> attachments = (List<Attachment>) execution.getVariable("list");
            if(CollectionUtils.isNotEmpty(attachments)){
                for (Attachment attachment : attachments) {
                    files.put(attachment.getRemark(),properties.getImagePath()+attachment.getRemark()+ StringPool.DOT+attachment.getAttachType());
                }
            }
        }

        List<Commodity> list =new ArrayList<>();
        list.add(commodity);

        Context context = new Context();
        context.setVariable("list",list);
        String text = "";
        if(commodity.getStage().equals(ProcessConstant.WRAPPER_NAME)){
            text= templateEngine.process("kdlOtherCommodity", context);
            //发送带附件的邮件
            MailUtils.sendMail(commodity.getStage(),text,mailProperties,mails,files);
        }else {
            text= templateEngine.process("kdlCommodity", context);
            //发送带附件的邮件
            MailUtils.sendMail(commodity.getStage(),text,mailProperties,mails,files);
        }



    }

    public String[] getEmails(Integer id){
        List<User> userList = userService.findUserByRoleId(id);
        List<String> userMails = new ArrayList<>();
        for (User user : userList) {
            if(StringUtils.isNotEmpty(user.getEmail())&&user.getAccept().equals("1")&&user.getStatus().equals("1")) {
                userMails.add(user.getEmail());
            }
        }
        return userMails.toArray(new String[0]);
    }


}
