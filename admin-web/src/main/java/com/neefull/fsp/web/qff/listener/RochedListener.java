package com.neefull.fsp.web.qff.listener;

import com.neefull.fsp.web.qff.config.SendMailProperties;
import com.neefull.fsp.web.qff.entity.Roche;
import com.neefull.fsp.web.qff.service.IRocheService;
import com.neefull.fsp.web.qff.utils.MailUtils;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

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


}
