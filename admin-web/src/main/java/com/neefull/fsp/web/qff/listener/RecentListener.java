package com.neefull.fsp.web.qff.listener;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.neefull.fsp.web.qff.config.ProcessInstanceProperties;
import com.neefull.fsp.web.qff.config.SendMailProperties;
import com.neefull.fsp.web.qff.config.TemplateProperties;
import com.neefull.fsp.web.qff.entity.Attachment;
import com.neefull.fsp.web.qff.entity.Commodity;
import com.neefull.fsp.web.qff.entity.Recent;
import com.neefull.fsp.web.qff.service.IAttachmentService;
import com.neefull.fsp.web.qff.service.ICommodityService;
import com.neefull.fsp.web.qff.service.IRecentService;
import com.neefull.fsp.web.qff.utils.FilePdfTemplate;
import com.neefull.fsp.web.qff.utils.MailUtils;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.system.entity.User;
import com.neefull.fsp.web.system.service.IUserService;
import com.sun.mail.util.MailSSLSocketFactory;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.security.GeneralSecurityException;
import java.util.*;

/**近效期QFF
 * @Author: chengchengchu
 * @Date: 2019/12/22  19:39
 */

@Service("recentListener")
public class RecentListener implements JavaDelegate {

    @Autowired
    private SendMailProperties mailProperties;
    @Autowired
    private TemplateProperties templateProperties;
    @Autowired
    private IRecentService recentService;
    @Autowired
    private FilePdfTemplate template;
    @Autowired
    private IUserService userService;
    @Autowired
    private IAttachmentService attachmentService;
    @Autowired
    private ProcessInstanceProperties properties;

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
                files.put(attachment.getRemark(),properties.getImagePath()+attachment.getRemark()+ StringPool.DOT+attachment.getAttachType());
            }
        }
//        Map<String,String> map = new HashMap<>();
//        String url = templateProperties.getConserveDownLoadPath()+ recent.getNumber()+".pdf";
//        template.createPdf(map,templateProperties.getConserveTemplatePath(),templateProperties.getConserveDownLoadPath(),url);

        StringBuilder content=new StringBuilder("<html><head></head><body><h3>您好。当前从系统获取数据如下：</h3>");
        content.append("<tr><h3>具体详情如下表所示:</h3></tr>");
        content.append("<table border='5' style='border:solid 1px #000;font-size=10px;'>");
        content.append("<tr style='background-color: #00A1DD'><td>康德乐物料号</td>" +
                "<td>罗氏物料号</td><td>产品物料号</td><td>有效期</td>" +
                "<td>批号</td><td>SAP批次</td><td>工厂</td><td>库位</td><td>数量</td></tr>");
        content.append("<tr><td>"+recent.getkMater()+"</td><td>"+recent.getrMater()+"</td>" +
                "<td>"+recent.getName()+"</td><td>"+recent.getUseLife()+"</td>" +
                "<td>"+recent.getBatch()+"</td><td>"+recent.getSapBatch()+"</td>" +
                "<td>"+recent.getFactory()+"</td><td>"+recent.getWareHouse()+"</td><td>"+recent.getNumber()+"</td></tr>");
        content.append("</table>");
        content.append("</body></html>");

        String text = content.toString();

        //发送带附件的邮件
        MailUtils.sendMail(text,mailProperties,mails,files);


    }


    public String[] getEmails(Integer id){
        List<User> userList = userService.findUserByRoleId(id);
        List<String> userMails = new ArrayList<>();
        for (User user : userList) {
            userMails.add(user.getEmail());
        }
        return userMails.toArray(new String[0]);
    }


}
