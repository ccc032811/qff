package com.neefull.fsp.web.qff.listener;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.neefull.fsp.web.qff.config.ProcessInstanceProperties;
import com.neefull.fsp.web.qff.config.SendMailProperties;
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

/**邮件监听器
 * @Author: chengchengchu
 * @Date: 2019/12/22  19:40
 */
@Slf4j
@Service("rochedListener")
public class RochedListener implements JavaDelegate {
    @Autowired
    private SendMailProperties mailProperties;
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

        Map<String,String> files = new HashMap<>();

        List<Roche> list = new ArrayList<>();
        list.add(roche);

        Context context = new Context();
        context.setVariable("list",list);
        String text = templateEngine.process("kdlRoche", context);

        //发送带附件的邮件
        MailUtils.sendMail(ProcessConstant.ROCHE_NAME,text,mailProperties,mails,files);

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
