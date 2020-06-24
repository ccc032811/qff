package com.neefull.fsp.web.qff.listener;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.neefull.fsp.web.qff.config.ProcessInstanceProperties;
import com.neefull.fsp.web.qff.config.SendMailProperties;
import com.neefull.fsp.web.qff.config.TemplateProperties;
import com.neefull.fsp.web.qff.entity.Attachment;
import com.neefull.fsp.web.qff.entity.Commodity;
import com.neefull.fsp.web.qff.entity.Recent;
import com.neefull.fsp.web.qff.entity.Roche;
import com.neefull.fsp.web.qff.service.IAttachmentService;
import com.neefull.fsp.web.qff.service.IRecentService;
import com.neefull.fsp.web.qff.service.IRocheService;
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
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * @Author: chengchengchu
 * @Date: 2019/12/22  19:40
 */
@Slf4j
@Service("rochedListener")
public class RochedListener implements JavaDelegate {
    @Autowired
    private SendMailProperties mailProperties;
    @Autowired
    private TemplateProperties templateProperties;
    @Autowired
    private IRocheService rocheService;
    @Autowired
    private FilePdfTemplate template;
    @Autowired
    private IUserService userService;
    @Autowired
    private IAttachmentService attachmentService;
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

        //生成pdf和获取附件的地址
        String businessKey = execution.getProcessBusinessKey();
        String starId = "";
        if (businessKey.startsWith(Roche.class.getSimpleName())) {
            if (StringUtils.isNotBlank(businessKey)) {
                //截取字符串
                starId = businessKey.split("\\:")[1].toString();
            }
        }
        Roche roche = rocheService.queryRocheById(Integer.parseInt(starId));
//        List<Attachment> attachments = (List<Attachment>) execution.getVariable("list");

        Map<String,String> files = new HashMap<>();
//        if(CollectionUtils.isNotEmpty(attachments)){
//            for (Attachment attachment : attachments) {
//                files.put(attachment.getRemark(),properties.getImagePath()+attachment.getRemark()+ StringPool.DOT+attachment.getAttachType());
//            }
//        }

//        Map<String,String> map = new HashMap<>();
//        String url = templateProperties.getConserveDownLoadPath()+ roche.getNumber()+".pdf";
//        template.createPdf(map,templateProperties.getConserveTemplatePath(),templateProperties.getConserveDownLoadPath(),url);

//        StringBuilder content=new StringBuilder("<html><head></head><body><h3>Dear KDL colleague,</h3>");
//        content.append("<tr><h3>您有待处理罗氏QFF如下，谢谢。</h3></tr>");
//        content.append("<table border='5' style='border:solid 1px #000;font-size=10px;'>");
//        content.append("<tr style='background-color: #00A1DD'><td>运输单号</td>" +
//                "<td>NO编号</td><td>Initiator发起人</td><td>申请日期</td>" +
//                "<td>Reason原因</td><td>产品/物料名称</td><td>产品/物料编号</td><td>批号/序列号</td><td>受影响数量</td>" +
//                "<td>期望完成日期</td></tr>");
//        content.append("<tr><td>" + roche.getTransport() + "</td><td>" + roche.getNumber() + "</td>" +
//                "<td>" + roche.getSponsor() + "</td><td>" + roche.getReqDate() + "</td>" +
//                "<td>" + roche.getReason() + "</td><td>" + roche.getMaterName() + "</td>" +
//                "<td>" + roche.getMaterCode() + "</td><td>" + roche.getBatch() + "</td><td>" + roche.getQuantity() + "</td><td>" + roche.getExceptDate() + "</td></tr>");
//        content.append("更多详细信息可登录系统查看，统一供应链门户: ");
//        content.append("</table>");
//        content.append("</body></html>");
//
//        String text = content.toString();
        List<Roche> list = new ArrayList<>();
        list.add(roche);

        Context context = new Context();
        context.setVariable("list",list);
        String text = templateEngine.process("kdlRoche", context);

        //发送带附件的邮件
        MailUtils.sendMail(text,mailProperties,mails,files);

    }

    public String[] getEmails(Integer id){
        List<User> userList = userService.findUserByRoleId(id);
        List<String> userMails = new ArrayList<>();
        for (User user : userList) {
            if(StringUtils.isNotEmpty(user.getEmail())) {
                userMails.add(user.getEmail());
            }
        }
        return userMails.toArray(new String[0]);
    }

}
