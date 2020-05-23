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
import java.io.File;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * 到货养护包装QFF邮件发送监听器
 *
 * @Author: chengchengchu
 * @Date: 2019/11/28  14:25
 */

@Service("commodityListener")
public class CommodityListener extends BaseController implements JavaDelegate{

    @Autowired
    private SendMailProperties mailProperties;
    @Autowired
    private TemplateProperties templateProperties;
    @Autowired
    private ICommodityService conserveService;
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

        List<Attachment> attachments = (List<Attachment>) execution.getVariable("list");

        Map<String,String> files = new HashMap<>();
        if(CollectionUtils.isNotEmpty(attachments)){
            for (Attachment attachment : attachments) {
                files.put(attachment.getRemark(),properties.getImagePath()+attachment.getRemark()+ StringPool.DOT+attachment.getAttachType());
            }
        }

//        Map<String,String> map = new HashMap<>();
//        String url = templateProperties.getConserveDownLoadPath()+ commodity.getNumber()+".pdf";
//        template.createPdf(map,templateProperties.getConserveTemplatePath(),templateProperties.getConserveDownLoadPath(),url);

        StringBuilder content=new StringBuilder("<html><head></head><body><h3>您好。当前从SAP系统获取数据如下：</h3>");
        content.append("<tr><h3>具体详情如下表所示:</h3></tr>");
        content.append("<table border='5' style='border:solid 1px #000;font-size=10px;'>");
        content.append("<tr style='background-color: #00A1DD'><td>运输单号</td>" +
                "<td>QFF编号</td><td>Plant工厂</td><td>KDL Material物料</td>" +
                "<td>康德乐SAP 批次</td><td>罗氏物料号</td><td>药厂物料号</td><td>罗氏批号</td><td>生产日期</td>" +
                "<td>有效期</td><td>异常总数</td><td>Remark箱号/备注</td></tr>");
        content.append("<tr><td>"+commodity.getTransport()+"</td><td>"+commodity.getNumber()+"</td>" +
                "<td>"+commodity.getPlant()+"</td><td>"+commodity.getkMater()+"</td>" +
                "<td>"+commodity.getkBatch()+"</td><td>"+commodity.getrMater()+"</td>" +
                "<td>"+commodity.getpMater()+"</td><td>"+commodity.getrBatch()+"</td><td>"+commodity.getManuDate()+"</td>" +
                "<td>"+commodity.getExpiryDate()+"</td><td>"+commodity.getQuarantine()+"</td><td>"+commodity.getRemark()+"</td></tr>");
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
