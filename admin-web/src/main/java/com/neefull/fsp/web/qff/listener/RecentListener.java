package com.neefull.fsp.web.qff.listener;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.neefull.fsp.web.qff.config.ProcessInstanceProperties;
import com.neefull.fsp.web.qff.config.SendMailProperties;

import com.neefull.fsp.web.qff.entity.Attachment;
import com.neefull.fsp.web.qff.entity.Recent;
import com.neefull.fsp.web.qff.service.IRecentService;
import com.neefull.fsp.web.qff.utils.MailUtils;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

/**近效期QFF
 * @Author: chengchengchu
 * @Date: 2019/12/22  19:39
 */
@Slf4j
@Service("recentListener")
public class RecentListener implements JavaDelegate {

    @Autowired
    private SendMailProperties mailProperties;
    @Autowired
    private IRecentService recentService;
    @Autowired
    private ProcessInstanceProperties properties;
    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public void execute(DelegateExecution execution) {
        String[] mails = null;

        if(execution.getCurrentActivityName().equals("罗氏")){
            //罗氏的邮箱
            mails = MailUtils.getEmails(98);
        }else if(execution.getCurrentActivityName().equals("康德乐")){
            //康德的邮箱
            mails = MailUtils.getEmails(87);
        }

        //生成pdf和获取附件的地址
        String businessKey = execution.getProcessBusinessKey();
        String starId = "";
        if (businessKey.startsWith(Recent.class.getSimpleName())) {
            if (StringUtils.isNotBlank(businessKey)) {
                //截取字符串
                starId = businessKey.split("\\:")[1].toString();
            }
        }
        Recent recent = recentService.queryRecentById(Integer.parseInt(starId));
        List<Attachment> attachments = (List<Attachment>) execution.getVariable("list");

        Map<String,String> files = new HashMap<>();
        if(CollectionUtils.isNotEmpty(attachments)){
            for (Attachment attachment : attachments) {
                files.put(attachment.getRemark()+ StringPool.DOT + attachment.getAttachType(),
                        properties.getImagePath()+attachment.getRemark()+ StringPool.DOT+attachment.getAttachType());
            }
        }

        List<Recent> list = new ArrayList<>();
        list.add(recent);

        Context context = new Context();
        context.setVariable("list",list);
        String text = "";
        if(recent.getStage().equals(ProcessConstant.RECENT_NAME)){
            text = templateEngine.process("kdlRecent", context);
            //发送带附件的邮件
            MailUtils.sendMail(recent.getStage(),text,mailProperties,mails,files);
        }else if(recent.getStage().equals(ProcessConstant.TEMPERATURE_NAME)){
            text = templateEngine.process("kdlTemperature", context);
            //发送带附件的邮件
            MailUtils.sendMail(recent.getStage(),text,mailProperties,mails,files);
        }

    }



}
