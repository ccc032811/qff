package com.neefull.fsp.web.job.task;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.neefull.fsp.web.qff.config.SendMailProperties;
import com.neefull.fsp.web.qff.entity.Commodity;
import com.neefull.fsp.web.qff.entity.Recent;
import com.neefull.fsp.web.qff.entity.Roche;
import com.neefull.fsp.web.qff.service.ICommodityService;
import com.neefull.fsp.web.qff.service.IRecentService;
import com.neefull.fsp.web.qff.service.IRocheService;
import com.neefull.fsp.web.qff.utils.MailUtils;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.system.entity.User;
import com.neefull.fsp.web.system.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

/**
 * @Author: chengchengchu
 * @Date: 2020/6/19  11:21
 */

@Slf4j
@Component
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class SelfProcessCommit {



    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private ICommodityService commodityService;
    @Autowired
    private IRecentService recentService;
    @Autowired
    private IRocheService rocheService;
    @Autowired
    private IUserService userService;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private SendMailProperties mailProperties;



    @Transactional
    public void taskCommit(String params){
        Integer dayCommit = Integer.parseInt(params);

        List<Task> taskList = taskService.createTaskQuery().list();

        if(CollectionUtils.isNotEmpty(taskList)){
            for (Task task : taskList) {
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();

                if(processInstance.getActivityId().equals(ProcessConstant.FOUR_STEP)){

                    if( processInstance.getBusinessKey().startsWith("Commodity")){
                        String id =  processInstance.getBusinessKey().split("\\:")[1];
                        Commodity commodity = commodityService.queryCommodityById(Integer.parseInt(id));
                        Integer days = getDifferDays(commodity.getCreateTime());
                        if(days>=dayCommit){
                            taskService.setAssignee(task.getId(),"康德乐自动完成");
                            taskService.complete(task.getId());
                            commodityService.updateCommodityStatus(commodity.getId(),ProcessConstant.HAS_FINISHED);
                        }

                    }else if( processInstance.getBusinessKey().startsWith("Recent")){
                        String id =  processInstance.getBusinessKey().split("\\:")[1];
                        Recent recent = recentService.queryRecentById(Integer.parseInt(id));
                        Integer days = getDifferDays(recent.getCreateTime());
                        if(days>=dayCommit){
                            taskService.setAssignee(task.getId(),"康德乐自动完成");
                            taskService.complete(task.getId());
                            recentService.updateRecentStatus(recent.getId(),ProcessConstant.HAS_FINISHED);
                        }
                    }
                }else if(processInstance.getActivityId().equals(ProcessConstant.THREE_STEP)){

                    if ( processInstance.getBusinessKey().startsWith("Roche")) {
                        String id =  processInstance.getBusinessKey().split("\\:")[1];
                        Roche roche = rocheService.queryRocheById(Integer.parseInt(id));
                        Integer days = getDifferDays(roche.getCreateTime());
                        if(days>=dayCommit){
                            taskService.setAssignee(task.getId(),"康德乐自动完成");
                            taskService.complete(task.getId());
                            rocheService.updateRocheStatus(roche.getId(),ProcessConstant.HAS_FINISHED);

                            // 发送邮件
                            Map<String,String> files = new HashMap<>();
                            String[] rocheMails = getEmails(86);
                            String[] kdlMails = getEmails(87);
                            String[] mails = new String[rocheMails.length+kdlMails.length];
                            System.arraycopy(rocheMails,0,mails,0,rocheMails.length);
                            System.arraycopy(kdlMails,0,mails,rocheMails.length,kdlMails.length);
                            List<Roche> list =new ArrayList<>();
                            list.add(roche);

                            Context context = new Context();
                            context.setVariable("list",list);
                            String text = templateEngine.process("rocheRoche", context);

                            //发送带附件的邮件
                            MailUtils.sendMail(null,text,mailProperties,mails,files);

                        }
                    }

                }
            }
        }

    }



    private Integer getDifferDays(Date createTime){
        long differDay = 0;
        if (createTime != null) {
            long startTime = createTime.getTime();
            long nowTime = System.currentTimeMillis();
            differDay = nowTime - startTime;
        }
        return (int) (differDay / (1000 * 60 * 60 * 24));
    }


    private String[] getEmails(Integer id){
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
