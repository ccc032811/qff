package com.neefull.fsp.web.qff.service.impl;

import com.neefull.fsp.web.qff.config.ProcessInstanceProperties;
import com.neefull.fsp.web.qff.config.SendMailProperties;
import com.neefull.fsp.web.qff.entity.*;
import com.neefull.fsp.web.qff.service.*;
import com.neefull.fsp.web.qff.utils.MailUtils;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.system.entity.User;
import com.neefull.fsp.web.system.service.IUserService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.validation.constraints.NotBlank;
import java.io.File;
import java.sql.Ref;
import java.util.*;

/**
 * @Author: chengchengchu
 * @Date: 2020/1/2  14:55
 */

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class ProcessServiceImpl implements IProcessService {


    @Autowired
    private ICommodityService commodityService;
    @Autowired
    private IAttachmentService attachmentService;
    @Autowired
    private IRecentService recentService;
    @Autowired
    private IRocheService rocheService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private ProcessInstanceProperties properties;
    @Autowired
    private IUserService userService;
    @Autowired
    private SendMailProperties mailProperties;
    @Autowired
    private TemplateEngine templateEngine;

    @Override
    @Transactional
    public void commitProcess(Object object, User user) {

        if(object instanceof Commodity){
            Commodity commodity = (Commodity) object;
            if(commodity.getId()==null){
                commodityService.addCommodity(commodity);
            }else {
                editCommodity(commodity);
            }
            String businessKey = Commodity.class.getSimpleName()+":"+commodity.getId();
            //启动流程
            startProcess(properties.getCommodityProcess(),businessKey);

            currentProcess(commodity,user);
            //更改状态审核中
            commodityService.updateCommodityStatus(commodity.getId(), ProcessConstant.UNDER_REVIEW);
        }else if(object instanceof Recent){
            Recent recent = (Recent) object;
            if(recent.getId()==null){
                recentService.addRecent(recent);
            }else {
                recentService.editRecent(recent);
            }
            String businessKey = Recent.class.getSimpleName()+":"+recent.getId();
            startProcess(properties.getRecentProcess(),businessKey);
            agreeCurrentProcess(recent,user);
            recentService.updateRecentStatus(recent.getId(), ProcessConstant.UNDER_REVIEW);
        }else if(object instanceof Roche){
            Roche roche = (Roche) object;
            rocheService.addRoche(roche);

            String businessKey = Roche.class.getSimpleName()+":"+roche.getId();
            startProcess(properties.getRocheProcess(),businessKey);
            agreeCurrentProcess(roche,user);
            //更改状态审核中
            rocheService.updateRocheStatus(roche.getId(), ProcessConstant.UNDER_REVIEW);
        }
    }

    @Transactional
    protected void startProcess(String definitionKey,String businessKey){
        runtimeService.startProcessInstanceByKey(definitionKey,businessKey);
    }


    @Override
    public List<String> getGroupId(Object object) {
        List<String> list = new ArrayList<>();
        Task task = null;
        String businessKey = getBusinessKey(object);

        if(StringUtils.isNotEmpty(businessKey)){
            task = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).singleResult();
        }

        if(task != null){
            List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask(task.getId());
            for (IdentityLink identityLink : identityLinksForTask) {
                list.add(identityLink.getUserId());
            }
        }
        return list;
    }

    @Transactional
    public void  currentProcess(Commodity commodity,User user){
        List<Attachment> attachments = null;
        String businessKey = Commodity.class.getSimpleName()+":"+commodity.getId();
        if(StringUtils.isNotEmpty(commodity.getImages())){
            attachments = addOrEditFiles(commodity, user);
        }
        ProcessInstance processInstance = getNewProcessInstance(businessKey, user,attachments);
        if(processInstance==null){
            commodityService.updateCommodityStatus(commodity.getId(),ProcessConstant.HAS_FINISHED);
        }
    }


    @Override
    @Transactional
    public void agreeCurrentProcess(Object object, User user) {
        //设置更新的时间
        if(object instanceof Commodity){
            Commodity commodity = (Commodity) object;
            editCommodity(commodity);

            currentProcess(commodity,user);

        }else if(object instanceof Recent){
            Recent recent = (Recent) object;
            recentService.editRecent(recent);
            List<Attachment> attachments = null;
            String businessKey = Recent.class.getSimpleName()+":"+recent.getId();
            if(StringUtils.isNotEmpty(recent.getImages())){
                attachments = addOrEditFiles(recent, user);
            }
            ProcessInstance processInstance = getNewProcessInstance(businessKey, user,attachments);
            if(processInstance==null){
                recentService.updateRecentStatus(recent.getId(),ProcessConstant.HAS_FINISHED);
            }

        }else if(object instanceof Roche){
            Roche roche = (Roche) object;
            rocheService.editRoche(roche);
            List<Attachment> attachments = null;
            if(StringUtils.isNotEmpty(roche.getImages())){
                attachments = addOrEditFiles(roche, user);
            }
            String businessKey = Roche.class.getSimpleName()+":"+roche.getId();
            ProcessInstance processInstance = getNewProcessInstance(businessKey, user,attachments);
            if(processInstance==null){
                rocheService.updateRocheStatus(roche.getId(),ProcessConstant.HAS_FINISHED);
            }

        }
    }

    @Transactional
    protected ProcessInstance getNewProcessInstance(String businessKey,User user,List<Attachment> attachments){
        Map<String,Object> map = new HashMap<>();
        map.put("list",attachments);
        Task task = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).singleResult();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
        String activityId = processInstance.getActivityId();
        if(activityId.equals(ProcessConstant.SIX_STEP)){
            taskService.setAssignee(task.getId(),"康德乐已提交");
        }else if(activityId.equals(ProcessConstant.FIVE_STEP)){
            taskService.setAssignee(task.getId(),"罗氏已提交");
        }else {
            taskService.claim(task.getId(),user.getUsername());
        }
        taskService.complete(task.getId(),map);
        return  queryProcessInstance(businessKey);
    }

    @Override
    public List<ProcessHistory> queryHistory(Object object) {
        List<ProcessHistory> list = new ArrayList<>();

        String businessKey = getBusinessKey(object);

        List<HistoricTaskInstance> taskInstances = queryHistoryList(businessKey);
        if(taskInstances!=null){
            for (HistoricTaskInstance taskInstance : taskInstances) {
                ProcessHistory processHistory = new ProcessHistory();
                processHistory.setName(taskInstance.getAssignee());
                if(taskInstance.getEndTime() == null){
                    processHistory.setDate(null);
                }else {
                    processHistory.setDate(DateFormatUtils.format(taskInstance.getEndTime(),"yyyy-MM-dd HH:mm:ss"));
                }
                list.add(processHistory);
            }
        }
        return list;
    }

    private List<HistoricTaskInstance> queryHistoryList(String businessKey){
        return historyService.createHistoricTaskInstanceQuery().processInstanceBusinessKey(businessKey).orderByHistoricTaskInstanceStartTime().asc().list();
    }


    @Override
    public List<Task> findTask(String name) {
        return queryTaskByUserName(name);

    }

    private List<Task> queryTaskByUserName(String name){
        return taskService.createTaskQuery().taskCandidateUser(name).list();
    }

    @Override
    @Transactional
    public void deleteInstance(Object object) {
        ProcessInstance processInstance = null;
        String businessKey = getBusinessKey(object);
        if(StringUtils.isNotEmpty(businessKey)){
            processInstance = queryProcessInstance(businessKey);
        }

        delete(processInstance);
    }

    private String getBusinessKey(Object object){
        String businessKey = "";
        if(object instanceof Commodity){
            Commodity commodity = (Commodity) object;
            businessKey = Commodity.class.getSimpleName()+":"+commodity.getId();
        }else if(object instanceof Recent){
            Recent recent = (Recent) object;
            businessKey = Recent.class.getSimpleName()+":"+recent.getId();

        }else if(object instanceof Roche) {
            Roche roche = (Roche) object;
            businessKey = Roche.class.getSimpleName() + ":" + roche.getId();
        }
        return businessKey;
    }

    @Override
    public Boolean queryProcessByKey(Object object) {
        ProcessInstance processInstance = null;
        String businessKey = "";
        if(object instanceof Commodity){
            Commodity commodity = (Commodity) object;
            businessKey = Commodity.class.getSimpleName()+":"+commodity.getId();
        }
        if(StringUtils.isNotEmpty(businessKey)){
            processInstance = queryProcessInstance(businessKey);
        }
        if(processInstance == null){
            return false;
        }
        return true;
    }

    @Transactional
    public List<Task> deleteProcessUser(User user){
        List<Task> list = taskService.createTaskQuery().list();
        if(CollectionUtils.isNotEmpty(list)){
            for (Task task : list) {
                taskService.deleteCandidateUser(task.getId(),user.getUsername());
            }
        }
        return list;
    }


    @Override
    @Transactional
    public void addProcessCommit(User user) {
        List<Task> list = deleteProcessUser(user);
        String[] split = user.getRoleId().split(",");
        for (String s : split) {
            if(s.equals("87")){
                if(CollectionUtils.isNotEmpty(list)){
                    for (Task task : list) {
                        ProcessInstance processInstance = getProcessInstanceById(task.getProcessInstanceId());
                        String activityId = processInstance.getActivityId();
                        String processDefinitionKey = processInstance.getProcessDefinitionKey();
                        if(activityId.equals(ProcessConstant.FOUR_STEP)&&processDefinitionKey.equals(properties.getCommodityProcess())){
                            taskService.addCandidateUser(task.getId(),user.getUsername());
                        }else if(activityId.equals(ProcessConstant.FOUR_STEP)&&processDefinitionKey.equals(properties.getRecentProcess())){
                            taskService.addCandidateUser(task.getId(),user.getUsername());
                        }else if(activityId.equals(ProcessConstant.THREE_STEP)&&processDefinitionKey.equals(properties.getRocheProcess())){
                            taskService.addCandidateUser(task.getId(),user.getUsername());
                        }
                    }
                }
            }else if(s.equals("86")){
                if(CollectionUtils.isNotEmpty(list)){
                    for (Task task : list) {
                        ProcessInstance processInstance = getProcessInstanceById(task.getProcessInstanceId());
                        String activityId = processInstance.getActivityId();
                        String processDefinitionKey = processInstance.getProcessDefinitionKey();
                        if(activityId.equals(ProcessConstant.THREE_STEP)&&processDefinitionKey.equals(properties.getCommodityProcess())){
                            taskService.addCandidateUser(task.getId(),user.getUsername());
                        }else if(activityId.equals(ProcessConstant.THREE_STEP)&&processDefinitionKey.equals(properties.getRecentProcess())){
                            taskService.addCandidateUser(task.getId(),user.getUsername());
                        }
                    }
                }
            }
        }
    }


    @Override
    public List<Commodity> queryCommodityTaskByName(List<Commodity> commodityList, User user) {
        List<Task> tasks = queryTaskByUserName(user.getUsername());
        for (Task task : tasks) {
            ProcessInstance processInstance = getProcessInstanceById(task.getProcessInstanceId());
            String businessKey = processInstance.getBusinessKey();
            String id = splitKey(businessKey, Commodity.class.getSimpleName());
            if(StringUtils.isNotEmpty(id)){
                for (Commodity commodity : commodityList) {
                    if(commodity.getId()==Integer.parseInt(id)){
                        commodity.setIsAllow(1);
                    }
                }
            }
        }
        return commodityList;
    }


    public ProcessInstance getProcessInstanceById(String processInstanceId){
        return  runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    }

    private String splitKey(String businessKey,String beanName){
        String id = "";
        if (businessKey.startsWith(beanName)){
            if (StringUtils.isNotBlank(businessKey)) {
                id = businessKey.split("\\:")[1].toString();
            }
        }
        return id;
    }


    @Override
    public List<Recent> queryRecentTaskByName(List<Recent> recentList, User user) {
        List<Task> tasks = queryTaskByUserName(user.getUsername());
        for (Task task : tasks) {
            ProcessInstance processInstance = getProcessInstanceById(task.getProcessInstanceId());
            String businessKey = processInstance.getBusinessKey();
            String id = splitKey(businessKey, Recent.class.getSimpleName());
            if(StringUtils.isNotEmpty(id)){
                for (Recent recent : recentList) {
                    if(recent.getId()==Integer.parseInt(id)){
                        recent.setIsAllow(1);
                    }
                }
            }
        }
        return recentList;
    }

    @Override
    public List<Roche> queryRocheTaskByName(List<Roche> rocheList, User user) {
        List<Task> tasks = queryTaskByUserName(user.getUsername());
        for (Task task : tasks) {
            ProcessInstance processInstance = getProcessInstanceById(task.getProcessInstanceId());
            String businessKey = processInstance.getBusinessKey();
            String id = splitKey(businessKey, Roche.class.getSimpleName());
            if(StringUtils.isNotEmpty(id)){
                for (Roche roche : rocheList) {
                    if(roche.getId()==Integer.parseInt(id)){
                        roche.setIsAllow(1);
                    }
                }
            }
        }
        return rocheList;
    }

    @Override
    public void deleteProcessCommit(String[] userIds) {
        if(ArrayUtils.isNotEmpty(userIds)){
            for (String userId : userIds) {
                User user = userService.findUserById(userId);
                deleteProcessUser(user);
            }
        }
    }

    @Override
    public List<String> findPrcessName(String username) {
        List<Task> list = findTask(username);
        Set<String> names = new HashSet<>();
        if(com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(list)){
            for (Task task : list) {
                ProcessInstance processInstance = getProcessInstanceById(task.getProcessInstanceId());
                if(processInstance.getBusinessKey().startsWith("Recent")){
                    names.add(ProcessConstant.RECENT_NAME);
                }else if(processInstance.getBusinessKey().startsWith("Roche")) {
                    names.add(ProcessConstant.ROCHE_NAME);
                }else {
                    String id = processInstance.getBusinessKey().split("\\:")[1];
                    Commodity commodity = commodityService.queryCommodityById(Integer.parseInt(id));
                    if(commodity!=null){
                        if(commodity.getStage().equals(ProcessConstant.DELIVERY_NAME)){
                            names.add(ProcessConstant.DELIVERY_NAME);
                        }else if(commodity.getStage().equals(ProcessConstant.CONSERVE_NAME)){
                            names.add(ProcessConstant.CONSERVE_NAME);
                        }else if(commodity.getStage().equals(ProcessConstant.EXPORT_NAME)){
                            names.add(ProcessConstant.EXPORT_NAME);
                        }else if(commodity.getStage().equals(ProcessConstant.WRAPPER_NAME)){
                            names.add(ProcessConstant.WRAPPER_NAME);
                        }else if(commodity.getStage().equals(ProcessConstant.REFUND_NAME)){
                            names.add(ProcessConstant.REFUND_NAME);
                        }
                    }
                }
            }
        }
        return new ArrayList<>(names);
    }

    @Override
    @Transactional
    public void alterCommodity(Commodity commodity, User currentUser) {
        StringBuffer alteration = new StringBuffer();
        String date = DateFormatUtils.format(new Date(),"yyyy-MM-dd");
        Commodity oldCommodity = commodityService.queryCommodityById(commodity.getId());
        if(!commodity.getBa().equals(oldCommodity.getBa())){
            alteration.append("BA: " +date+ "。由  "+oldCommodity.getBa()+"修改为"+commodity.getBa()+"  。 ");
        }
        if(!commodity.getReason().equals(oldCommodity.getReason())){
            alteration.append("QFF原因: " +date+ "。由  "+oldCommodity.getReason()+"修改为"+commodity.getReason()+"  。 ");
        }
        if(!commodity.getCompNumber().equals(oldCommodity.getCompNumber())){
            alteration.append("投诉编号: " +date+ "。由  "+oldCommodity.getCompNumber()+"修改为"+commodity.getCompNumber()+"  。 ");
        }
        if(!commodity.getrConf().equals(oldCommodity.getrConf())){
            alteration.append("罗氏处理意见: " +date+ "。由  "+oldCommodity.getrConf()+"修改为"+commodity.getrConf()+"  。 ");
        }
        if(!commodity.getCheckResult().equals(oldCommodity.getCheckResult())){
            alteration.append("仪器工程师检查结果: " +date+ "。由  "+oldCommodity.getCheckResult()+"修改为"+commodity.getCheckResult()+"  。 ");
        }
        if(!commodity.getRemark().equals(oldCommodity.getRemark())){
            alteration.append("备注: " +date+ "。由  "+oldCommodity.getRemark()+"修改为"+commodity.getRemark()+"  。 ");
        }
        commodity.setAlteration(oldCommodity.getAlteration()+"  "+alteration.toString());
        commodityService.editCommodity(commodity);
        if(StringUtils.isNotEmpty(commodity.getImages())){
            addOrEditFiles(commodity,currentUser);
        }
        String[] mails = getEmails(87);

        List<Commodity> list =new ArrayList<>();
        commodity.setAlteration(alteration.toString());
        list.add(commodity);
        Map<String,String> files = new HashMap<>();

        Context context = new Context();
        context.setVariable("list",list);
        String text = templateEngine.process("kdlCommodity", context);

        //发送带附件的邮件
        MailUtils.sendMail(text,mailProperties,mails,files);
    }

    @Override
    @Transactional
    public void alterRecent(Recent recent, User currentUser) {
        StringBuffer alteration = new StringBuffer();
        String date = DateFormatUtils.format(new Date(),"yyyy-MM-dd");
        Recent oldRecent = recentService.queryRecentById(recent.getId());
        if(!recent.getrConf().equals(oldRecent.getrConf())){
            alteration.append("罗氏处理意见: " +date+ "。由  "+oldRecent.getrConf()+"修改为"+recent.getrConf()+"  。 ");
        }
        recent.setAlteration(oldRecent.getAlteration() +"  "+alteration.toString());
        recentService.editRecent(recent);
        if(StringUtils.isNotEmpty(recent.getImages())){
            addOrEditFiles(recent,currentUser);
        }
        String[] mails = getEmails(87);

        List<Recent> list =new ArrayList<>();
        recent.setAlteration(alteration.toString());
        list.add(recent);
        Map<String,String> files = new HashMap<>();

        Context context = new Context();
        context.setVariable("list",list);
        String text = templateEngine.process("kdlRecent", context);

        //发送带附件的邮件
        MailUtils.sendMail(text,mailProperties,mails,files);

    }

    @Override
    @Transactional
    public void alterRoche(Roche roche, User currentUser) {
        StringBuffer alteration = new StringBuffer();
        String date = DateFormatUtils.format(new Date(),"yyyy-MM-dd");
        Roche oldRoche = rocheService.queryRocheById(roche.getId());
        if(!roche.getReason().equals(oldRoche.getReason())){
            alteration.append("原因: " +date+ "。由  "+oldRoche.getReason()+"修改为"+roche.getReason()+"  。 ");
        }
        if(!roche.getActions().equals(oldRoche.getActions())){
            alteration.append("行动: " +date+ "。由  "+oldRoche.getActions()+"修改为"+roche.getActions()+"  。 ");
        }
        if(!roche.getCompleteDate().equals(oldRoche.getCompleteDate())){
            alteration.append("实际日期: " +date+ "。由  "+oldRoche.getCompleteDate()+"修改为"+roche.getCompleteDate()+"  。 ");
        }
        if(!roche.getFollow().equals(oldRoche.getFollow())){
            alteration.append("后续行动: " +date+ "。由  "+oldRoche.getFollow()+"修改为"+roche.getFollow()+"  。 ");
        }
        if(!roche.getActualDate().equals(oldRoche.getActualDate())){
            alteration.append("实际日期: " +date+ "。由  "+oldRoche.getActualDate()+"修改为"+roche.getActualDate()+"  。 ");
        }
        roche.setAlteration(oldRoche.getAlteration() +"  "+alteration.toString());
        rocheService.editRoche(roche);
        String[] mails = getEmails(87);

        List<Roche> list =new ArrayList<>();
        roche.setAlteration(alteration.toString());
        list.add(roche);
        Map<String,String> files = new HashMap<>();

        Context context = new Context();
        context.setVariable("list",list);
        String text = templateEngine.process("kdlRoche", context);

        //发送带附件的邮件
        MailUtils.sendMail(text,mailProperties,mails,files);


    }


    private ProcessInstance queryProcessInstance(String businessKey){
        return runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();

    }

    @Transactional
    protected void delete(ProcessInstance processInstance){
        if(processInstance!=null){
            runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(),null);
        }
    }

    @Transactional
    public List<Attachment> addOrEditFiles (Object object ,User user) {
        String image = "";
        List<Attachment> list = new ArrayList<>();
        if (object instanceof Commodity) {
            Commodity commodity = (Commodity) object;
            list = saveAttachment(commodity.getNumber(),commodity.getStage(), commodity.getImages(),user);

        } else if (object instanceof Recent) {
            Recent recent = (Recent) object;
            list = saveAttachment(String.valueOf(recent.getId()),ProcessConstant.RECENT_NAME,recent.getImages(),user);
        } else if (object instanceof Roche) {
            Roche roche = (Roche) object;
            list = saveAttachment(String.valueOf(roche.getId()),ProcessConstant.ROCHE_NAME,roche.getImages(),user);
        }
        return list;
    }

    @Transactional
    public List<Attachment> saveAttachment(String id ,String type ,String image, User user){
        String[] images = image.split(",");
        List<Attachment> list = new ArrayList<>();
        for (String file : images) {
            if(StringUtils.isNotEmpty(file)){
                Attachment attachment = new Attachment();
                attachment.setQffId(id);
                attachment.setQffType(type);
                attachment.setSource(2);
                attachment.setEnable(1);
                attachment.setVest(user.getDeptName());
                attachment.setAttachType(file.substring(file.lastIndexOf(".")+1 ,file.length()));
                attachment.setRemark(file.substring(0,file.lastIndexOf(".")));
                File filePath = new File(properties.getImagePath() + file);
                attachment.setAttachSize(filePath.length()/1024);
                attachmentService.addAttachment(attachment);
                list.add(attachment);
            }
        }
        return list;
    }


    @Transactional
    protected void editCommodity(Commodity commodity){
        String format = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
        commodity.setRepTime(format);
        commodityService.editCommodity(commodity);
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
