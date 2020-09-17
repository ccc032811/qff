package com.neefull.fsp.web.qff.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.neefull.fsp.web.qff.config.ProcessInstanceProperties;
import com.neefull.fsp.web.qff.config.SendMailProperties;
import com.neefull.fsp.web.qff.entity.*;
import com.neefull.fsp.web.qff.service.*;
import com.neefull.fsp.web.qff.utils.MailUtils;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.system.entity.User;
import com.neefull.fsp.web.system.service.IUserService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
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
    @Autowired
    private RepositoryService repositoryService;



    @Override
    @Transactional
    public void commitProcess(Object object, User user) {

        List<Attachment> attachments = new ArrayList<>();

        if(object instanceof Commodity){
            Commodity commodity = (Commodity) object;
            String businessKey = getBusinessKey(commodity);

            if(commodity.getId()==null){
                commodityService.addCommodity(commodity);

                startProcess(commodity);

                if(StringUtils.isNotEmpty(commodity.getImages())){
                    addOrEditFiles(commodity, user);
                }
                if(commodity.getStage().equals(ProcessConstant.WRAPPER_NAME)){
                    Map<String, String> files = new HashMap<>();
                    String[] rocheMails = getEmails(86);

                    List<Commodity> commodityList = new ArrayList<>();
                    commodityList.add(commodity);

                    Context context = new Context();
                    context.setVariable("list", commodityList);
                    String text = templateEngine.process("rocheOtherCommodity", context);

                    //发送带附件的邮件
                    MailUtils.sendMail(text, mailProperties, rocheMails, files);
                }

            }else {
                editCommodity(commodity);

                if(StringUtils.isNotEmpty(commodity.getImages())){
                    attachments = addOrEditFiles(commodity, user);
                }

                agreeProcess(businessKey,user,attachments);
                if(queryProcessInstance(businessKey)==null){
                    commodityService.updateCommodityStatus(commodity.getId(),ProcessConstant.HAS_FINISHED);
                }
            }

        }else if(object instanceof Recent){
            Recent recent = (Recent) object;

            Map<String,String> files = new HashMap<>();
            String businessKey = getBusinessKey(recent);

            if(recent.getId()==null){
                recent.setStartDate(DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
                recentService.addRecent(recent);

                if(StringUtils.isNotEmpty(recent.getImages())){
                    attachments = addOrEditFiles(recent, user);
                    for (Attachment attachment : attachments) {
                        files.put(attachment.getRemark()+ StringPool.DOT + attachment.getAttachType(),
                                properties.getImagePath() + attachment.getRemark() + StringPool.DOT + attachment.getAttachType());
                    }
                }

                businessKey = getBusinessKey(recent);
                startProcess(properties.getRecentProcess(),businessKey);

                Task task = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).singleResult();
                taskService.setAssignee(task.getId(),"康德乐发起申请");
                taskService.complete(task.getId());

                recentService.updateRecentStatus(recent.getId(), ProcessConstant.UNDER_REVIEW);

                String[] rocheMails = getEmails(86);
                List<Recent> recentList =new ArrayList<>();
                recentList.add(recent);

                Context context = new Context();
                context.setVariable("list",recentList);
                String text = "";
                if(recent.getStage().equals(ProcessConstant.RECENT_NAME)){
                    text = templateEngine.process("rocheRecent", context);

                }else if(recent.getStage().equals(ProcessConstant.TEMPERATURE_NAME)){
                    text = templateEngine.process("rocheTemperature", context);
                }

                MailUtils.sendMail(text,mailProperties,rocheMails,files);
            }else {
                editCommodity(recent);

                if(StringUtils.isNotEmpty(recent.getImages())){
                    attachments = addOrEditFiles(recent, user);
                }
                agreeProcess(businessKey,user,attachments);
                if(queryProcessInstance(businessKey)==null){
                    recentService.updateRecentStatus(recent.getId(),ProcessConstant.HAS_FINISHED);
                }
            }

        }else if(object instanceof Roche){
            Roche roche = (Roche) object;
            String businessKey = getBusinessKey(roche);

            if(roche.getId()==null){
                rocheService.addRoche(roche);

                if(StringUtils.isNotEmpty(roche.getImages())){
                    attachments = addOrEditFiles(roche, user);
                }
                businessKey = getBusinessKey(roche);
                startProcess(properties.getRocheProcess(),businessKey);

                Task task = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).singleResult();
                taskService.setAssignee(task.getId(),"罗氏发起申请");
                taskService.complete(task.getId());

                rocheService.updateRocheStatus(roche.getId(), ProcessConstant.UNDER_REVIEW);
            }else {
                rocheService.editRoche(roche);

                if(StringUtils.isNotEmpty(roche.getImages())){
                    attachments = addOrEditFiles(roche, user);
                }

                agreeProcess(businessKey,user,attachments);
                if(queryProcessInstance(businessKey)==null){
                    rocheService.updateRocheStatus(roche.getId(),ProcessConstant.HAS_FINISHED);
                    // 发送邮件
                    Map<String,String> files = new HashMap<>();
                    String[] rocheMails = getEmails(86);
                    String[] kdlMails = getEmails(87);
                    String[] mails = new String[rocheMails.length+kdlMails.length];
                    System.arraycopy(rocheMails,0,mails,0,rocheMails.length);
                    System.arraycopy(kdlMails,0,mails,rocheMails.length,kdlMails.length);


                    List<Roche> rocheList =new ArrayList<>();
                    rocheList.add(roche);

                    Context context = new Context();
                    context.setVariable("list",rocheList);
                    String text = templateEngine.process("rocheRoche", context);

                    //发送带附件的邮件
                    MailUtils.sendMail(text,mailProperties,mails,files);

                }
            }


        }
    }

    @Transactional
    @Override
    public void startProcess(Commodity commodity){
        String businessKey = getBusinessKey(commodity);
        startProcess(properties.getCommodityProcess(),businessKey);

        Task task = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).singleResult();
        taskService.setAssignee(task.getId(),"康德乐发起申请");
        taskService.complete(task.getId());

        commodityService.updateCommodityStatus(commodity.getId(), ProcessConstant.UNDER_REVIEW);
    }


    @Transactional
    protected void agreeProcess(String businessKey,User user,List<Attachment> attachments){
        Map<String,Object> map = new HashMap<>();
        map.put("list",attachments);
        List<Task> list = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).list();
        if(CollectionUtils.isNotEmpty(list)){
            for (Task task : list) {
                Set taskCandidate = getTaskCandidate(task.getId());
                if(taskCandidate.contains(user.getUsername())){
                    taskService.claim(task.getId(),user.getUsername());
                    taskService.complete(task.getId(),map);
                }
            }
        }
    }


    @Transactional
    protected void editCommodity(Object object){

        if(object instanceof Commodity) {
            Commodity commodity = (Commodity) object;
            String businessKey = getBusinessKey(commodity);
            Boolean editTime = editTime(businessKey);
            if (editTime) {
                commodity.setRepTime(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            }
            commodityService.editCommodity(commodity);
        }else if(object instanceof Recent){
            Recent recent = (Recent) object;
            String businessKey = getBusinessKey(recent);
            Boolean editTime = editTime(businessKey);
            if (editTime) {
                recent.setRepDate(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            }
            recentService.editRecent(recent);
        }
    }

    private Boolean editTime(String businessKey){
        ProcessInstance processInstance = queryProcessInstance(businessKey);
        String activityId = processInstance.getActivityId();
        return activityId.equals(ProcessConstant.THREE_STEP);
    }

    private Set getTaskCandidate(String taskId){
        Set<String> user = new HashSet<String>();
        List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask(taskId);
        if(CollectionUtils.isNotEmpty(identityLinksForTask)){
            for (IdentityLink identityLink : identityLinksForTask) {
                if(identityLink.getUserId()!=null){
                    user.add(identityLink.getUserId());
                }
            }
        }
        return user;
    }


    @Transactional
    protected void startProcess(String definitionKey,String businessKey){
        runtimeService.startProcessInstanceByKey(definitionKey,businessKey);
    }


    @Override
    public List<String> getGroupId(Object object) {
        List<String> list = new ArrayList<>();
        List<Task> taskList = null;
        String businessKey = getBusinessKey(object);

        if(StringUtils.isNotEmpty(businessKey)){
            taskList = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).list();
        }

        if(CollectionUtils.isNotEmpty(taskList)){
            for (Task task : taskList) {
                List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask(task.getId());
                for (IdentityLink identityLink : identityLinksForTask) {
                    list.add(identityLink.getUserId());
                }
            }

        }
        return list;
    }


    @Override
    public  Map<String,ProcessHistory> queryHistory(Object object) {
        Map<String,ProcessHistory> map = new HashMap<>();

        String businessKey = getBusinessKey(object);

        List<HistoricTaskInstance> taskInstances = queryHistoryList(businessKey);

        if(taskInstances!=null){
            for (HistoricTaskInstance taskInstance : taskInstances) {

                if(taskInstance.getEndTime() != null){
                    ProcessHistory processHistory = new  ProcessHistory();
                    processHistory.setName(taskInstance.getAssignee());
                    processHistory.setDate(DateFormatUtils.format(taskInstance.getEndTime(),"yyyy-MM-dd HH:mm:ss"));
                    map.put(taskInstance.getTaskDefinitionKey(),processHistory);
                }
            }
        }




        return map;
    }

    private List<HistoricTaskInstance> queryHistoryList(String businessKey){
        return historyService.createHistoricTaskInstanceQuery().processInstanceBusinessKey(businessKey)
                .orderByHistoricTaskInstanceEndTime().asc().list();
    }


    @Override
    public List<String> findTask(String name) {
        List<Task> list = queryTaskByUserName(name);
        List<String> names = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(list)){
            for (Task task : list) {
                ProcessInstance processInstance = getProcessInstanceById(task.getProcessInstanceId());
                if(processInstance.getBusinessKey().startsWith("Recent")){
                    String id = processInstance.getBusinessKey().split("\\:")[1];
                    Recent recent = recentService.queryRecentById(Integer.parseInt(id));
                    if(recent.getStage().equals(ProcessConstant.RECENT_NAME)){
                        names.add("recent");
                    }else if(recent.getStage().equals(ProcessConstant.TEMPERATURE_NAME)){
                        names.add("temperature");
                    }
                }else if(processInstance.getBusinessKey().startsWith("Roche")) {
                    names.add("roche");
                }else {
                    String id = processInstance.getBusinessKey().split("\\:")[1];
                    Commodity commodity = commodityService.queryCommodityById(Integer.parseInt(id));
                    if(commodity!=null){
                        if(commodity.getStage().equals(ProcessConstant.DELIVERY_NAME)){
                            names.add("delivery");
                        }else if(commodity.getStage().equals(ProcessConstant.CONSERVE_NAME)){
                            names.add("conserve");
                        }
//                        else if(commodity.getStage().equals(ProcessConstant.PACKAGE_NAME)){
//                            names.add("package");
//                        }else if(commodity.getStage().equals(ProcessConstant.EXPORT_NAME)){
//                            names.add("export");
//                        }
                        else if(commodity.getStage().equals(ProcessConstant.WRAPPER_NAME)){
                            names.add("wrapper");
                        }else if(commodity.getStage().equals(ProcessConstant.REFUND_NAME)){
                            names.add("refund");
                        }
                    }
                }
            }
        }

        return names;
    }

    private List<Task> queryTaskByUserName(String name){
        return taskService.createTaskQuery().taskCandidateUser(name).list();
    }


    @Override
    @Transactional
    public void deleteInstance(Object object) {
        ProcessInstance processInstance = null;
        String businessKey = getBusinessKey(object);
        deleteAtt(object);
        if(StringUtils.isNotEmpty(businessKey)){
            processInstance = queryProcessInstance(businessKey);
        }
        delete(processInstance);
    }


    @Transactional
    public void  deleteAtt(Object object){
        if(object instanceof Commodity){
            Commodity commodity = (Commodity) object;
            attachmentService.deleteByNumber(commodity.getNumber(),commodity.getStage());
        }else if(object instanceof Recent){
            Recent recent = (Recent) object;
            attachmentService.deleteByNumber(String.valueOf(recent.getId()),ProcessConstant.RECENT_NAME);
        }else if(object instanceof Roche) {
            Roche roche = (Roche) object;
            attachmentService.deleteByNumber(String.valueOf(roche.getId()),ProcessConstant.ROCHE_NAME);
        }
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


    private ProcessDefinitionEntity getProcessDefinitionEntity(String processDefinitionId){
        return (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefinitionId);
    }


    private Boolean getHistoricTaskInstance(String processInstanceId,String process){
        boolean complete = true;
        List<HistoricActivityInstance> list = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        if(CollectionUtils.isNotEmpty(list)){
            for (HistoricActivityInstance his : list) {
                if(his.getActivityId().equals(process)){
                    if(StringUtils.isNotEmpty(his.getAssignee())){
                        complete = false;
                    }
                }
            }
        }
        return complete;

    }


    @Override
    @Transactional
    public void addProcessCommit(User user) {
        List<Task> list = deleteProcessUser(user);
        String roleId = user.getRoleId();
        if(roleId.contains("87")){
            if(CollectionUtils.isNotEmpty(list)){
                for (Task task : list) {

                    ProcessInstance processInstance = getProcessInstanceById(task.getProcessInstanceId());
                    String processDefinitionKey = processInstance.getProcessDefinitionKey();

                    if(processDefinitionKey.equals(properties.getCommodityProcess())
                            &&task.getTaskDefinitionKey().equals(ProcessConstant.FOUR_STEP)){
                        taskService.addCandidateUser(task.getId(), user.getUsername());

                    }else if(processDefinitionKey.equals(properties.getRecentProcess())
                            &&task.getTaskDefinitionKey().equals(ProcessConstant.FOUR_STEP)){
                        taskService.addCandidateUser(task.getId(),user.getUsername());
                    }else if(processDefinitionKey.equals(properties.getRocheProcess())
                            &&task.getTaskDefinitionKey().equals(ProcessConstant.THREE_STEP)){
                        taskService.addCandidateUser(task.getId(),user.getUsername());
                    }
                }
            }
        }else if(roleId.contains("86")){
            if(CollectionUtils.isNotEmpty(list)){
                for (Task task : list) {
                    ProcessInstance processInstance = getProcessInstanceById(task.getProcessInstanceId());
                    String processDefinitionKey = processInstance.getProcessDefinitionKey();

                    if(processDefinitionKey.equals(properties.getCommodityProcess())) {
                        if (task.getTaskDefinitionKey().equals(ProcessConstant.THREE_STEP)
                                ||task.getTaskDefinitionKey().equals(ProcessConstant.TWELVE_STEP)){
                            taskService.addCandidateUser(task.getId(), user.getUsername());
                        }
                    }else if(processDefinitionKey.equals(properties.getRecentProcess())) {
                        if(task.getTaskDefinitionKey().equals(ProcessConstant.THREE_STEP)
                                ||task.getTaskDefinitionKey().equals(ProcessConstant.EIGHT_STEP)){
                            taskService.addCandidateUser(task.getId(), user.getUsername());
                        }
                    }else if(task.getTaskDefinitionKey().equals(ProcessConstant.SIX_STEP)
                            &&processDefinitionKey.equals(properties.getRocheProcess())){
                        taskService.addCandidateUser(task.getId(),user.getUsername());
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
                    if(commodity.getId()==Integer.parseInt(id)&&commodity.getStatus()==2){
                        commodity.setIsAllow(1);
                    }
                }
            }
        }
        return commodityList;
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
    @Transactional
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
        List<Task> list = queryTaskByUserName(username);
        Set<String> names = new HashSet<>();
        if(CollectionUtils.isNotEmpty(list)){
            for (Task task : list) {
                ProcessInstance processInstance = getProcessInstanceById(task.getProcessInstanceId());
                if(processInstance.getBusinessKey().startsWith("Recent")){
                    String id = processInstance.getBusinessKey().split("\\:")[1];
                    Recent recent = recentService.queryRecentById(Integer.parseInt(id));
                    if(recent.getStage().equals(ProcessConstant.RECENT_NAME)){
                        names.add(ProcessConstant.RECENT_NAME);
                    }else if(recent.getStage().equals(ProcessConstant.TEMPERATURE_NAME)){
                        names.add(ProcessConstant.TEMPERATURE_NAME);
                    }
                }else if(processInstance.getBusinessKey().startsWith("Roche")) {
                    names.add(ProcessConstant.ROCHE_NAME);
                }else {
                    String id = processInstance.getBusinessKey().split("\\:")[1];
                    Commodity commodity = commodityService.queryCommodityById(Integer.parseInt(id));
                    if(commodity!=null){
                        if(commodity.getStage().equals(ProcessConstant.DELIVERY_NAME)){
                            names.add(ProcessConstant.DELIVERY_NAME);
                        }else if(commodity.getStage().equals(ProcessConstant.CONSERVE_NAME)){
                            names.add(ProcessConstant.STORE_PACKAGE_EXPORT);
                        }
//                        else if(commodity.getStage().equals(ProcessConstant.PACKAGE_NAME)) {
//                            names.add(ProcessConstant.PACKAGE_NAME);
//                        }else if(commodity.getStage().equals(ProcessConstant.EXPORT_NAME)){
//                            names.add(ProcessConstant.EXPORT_NAME);
//                        }
                        else if(commodity.getStage().equals(ProcessConstant.WRAPPER_NAME)){
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
        String date = DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss");
        commodity.setRepTime(date);
        Commodity oldCommodity = commodityService.queryCommodityById(commodity.getId());
        if(!commodity.getStage().equals(ProcessConstant.WRAPPER_NAME)){
            if(!commodity.getBa().equals(oldCommodity.getBa())){
                alteration.append("BA:" +date+ " 由 "+oldCommodity.getBa()+" 修改为 "+commodity.getBa()+"  。 ");
            }
        }
        if(!commodity.getReason().equals(oldCommodity.getReason())){
            alteration.append("QFF原因:" +date+ " 由 "+oldCommodity.getReason()+" 修改为 "+commodity.getReason()+"  。 ");
        }
        if(!commodity.getCompNumber().equals(oldCommodity.getCompNumber())){
            alteration.append("投诉编号:" +date+ " 由 "+oldCommodity.getCompNumber()+" 修改为 "+commodity.getCompNumber()+"  。 ");
        }
        if(!commodity.getrConf().equals(oldCommodity.getrConf())){
            alteration.append("罗氏处理意见:" +date+ " 由 "+oldCommodity.getrConf()+" 修改为 "+commodity.getrConf()+"  。 ");
        }
        if(!commodity.getCheckResult().equals(oldCommodity.getCheckResult())){
            alteration.append("仪器工程师检查结果:" +date+ " 由 "+oldCommodity.getCheckResult()+" 修改为 "+commodity.getCheckResult()+"  。 ");
        }
        if(!commodity.getRemark().equals(oldCommodity.getRemark())){
            alteration.append("备注:" +date+ " 由 "+oldCommodity.getRemark()+" 修改为 "+commodity.getRemark()+"  。 ");
        }
        if(StringUtils.isNotEmpty(oldCommodity.getAlteration())) {
            commodity.setAlteration(oldCommodity.getAlteration() + "  " + alteration.toString());
        }else {
            commodity.setAlteration(alteration.toString());
        }
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
        List<Attachment> attachments = new ArrayList<>();
        Map<String,String> files = new HashMap<>();

        StringBuffer alteration = new StringBuffer();
        String date = DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss");
        recent.setRepDate(date);
        Recent oldRecent = recentService.queryRecentById(recent.getId());
        if(!recent.getrConf().equals(oldRecent.getrConf())&&recent.getStage().equals(ProcessConstant.RECENT_NAME)){
            alteration.append("罗氏处理意见:" +date+ " 由 "+oldRecent.getrConf()+" 修改为 "+recent.getrConf()+"  。 ");
        }else if(recent.getStage().equals(ProcessConstant.TEMPERATURE_NAME)){
            alteration.append("罗氏处理意见:" +date+ " 由 "+oldRecent.getrConf()+" 修改为 "+recent.getrConf()+"  。 ");
        }
        if(StringUtils.isNotEmpty(oldRecent.getAlteration())) {
            recent.setAlteration(oldRecent.getAlteration() + "  " + alteration.toString());
        }else {
            recent.setAlteration(alteration.toString());
        }
        recentService.editRecent(recent);
        if(StringUtils.isNotEmpty(recent.getImages())){
            attachments = addOrEditFiles(recent, currentUser);
            for (Attachment attachment : attachments) {
                files.put(attachment.getRemark()+ StringPool.DOT + attachment.getAttachType(),
                        properties.getImagePath() + attachment.getRemark() + StringPool.DOT + attachment.getAttachType());
            }
        }
        String[] mails = getEmails(87);

        List<Recent> list =new ArrayList<>();
        recent.setAlteration(alteration.toString());
        list.add(recent);

        Context context = new Context();
        context.setVariable("list",list);
        String text = "";
        if(recent.getStage().equals(ProcessConstant.RECENT_NAME)){
            text = templateEngine.process("kdlRecent", context);

        }else if(recent.getStage().equals(ProcessConstant.TEMPERATURE_NAME)){
            text = templateEngine.process("kdlTemperature", context);
        }

        //发送带附件的邮件
        MailUtils.sendMail(text,mailProperties,mails,files);

    }

    @Override
    @Transactional
    public void alterRoche(Roche roche, User currentUser) {
        StringBuffer alteration = new StringBuffer();
        String date = DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss");
        roche.setCreateTime(new Date());
        Roche oldRoche = rocheService.queryRocheById(roche.getId());
        if(!roche.getReason().equals(oldRoche.getReason())){
            alteration.append("原因:" +date+ " 由 "+oldRoche.getReason()+" 修改为 "+roche.getReason()+"  。 ");
        }
        if(!roche.getActions().equals(oldRoche.getActions())){
            alteration.append("行动:" +date+ " 由 "+oldRoche.getActions()+" 修改为 "+roche.getActions()+"  。 ");
        }
        if(!roche.getCompleteDate().equals(oldRoche.getCompleteDate())){
            alteration.append("实际日期:" +date+ " 由 "+oldRoche.getCompleteDate()+" 修改为 "+roche.getCompleteDate()+"  。 ");
        }
        if(!roche.getFollow().equals(oldRoche.getFollow())){
            alteration.append("后续行动:" +date+ " 由 "+oldRoche.getFollow()+" 修改为 "+roche.getFollow()+"  。 ");
        }
        if(!roche.getRemark().equals(oldRoche.getRemark())){
            alteration.append("备注:" +date+ " 由 "+oldRoche.getRemark()+" 修改为 "+roche.getRemark()+"  。 ");
        }
        if(StringUtils.isNotEmpty(oldRoche.getAlteration())){
            roche.setAlteration(oldRoche.getAlteration() +"  "+alteration.toString());
        }else {
            roche.setAlteration(alteration.toString());
        }

        rocheService.editRoche(roche);
        if(StringUtils.isNotEmpty(roche.getImages())){
            addOrEditFiles(roche,currentUser);
        }
        String[] mails = getEmails(87);

        List<Roche> list =new ArrayList<>();
        roche.setAlteration(alteration.toString());
        list.add(roche);
        Map<String,String> files = new HashMap<>();

        Context context = new Context();
        context.setVariable("list",list);
        String text = templateEngine.process("kdlRoche", context);

        MailUtils.sendMail(text,mailProperties,mails,files);

    }


    private ProcessInstance getProcessInstanceById(String processInstanceId){
        return  runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    }

    private ProcessInstance queryProcessInstance(String businessKey){
        return runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();

    }

    @Transactional
    protected void delete(ProcessInstance processInstance){
        if(processInstance!=null){
            runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(),null);
            historyService.deleteHistoricProcessInstance(processInstance.getProcessInstanceId());
        }
    }

    @Transactional
    public List<Attachment> addOrEditFiles (Object object ,User user) {
        List<Attachment> list = new ArrayList<>();
        if (object instanceof Commodity) {
            Commodity commodity = (Commodity) object;
            list = saveAttachment(commodity.getNumber(),commodity.getStage(), commodity.getImages(),user);
        } else if (object instanceof Recent) {
            Recent recent = (Recent) object;
            list = saveAttachment(String.valueOf(recent.getId()),recent.getStage(),recent.getImages(),user);
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
                attachment.setStatus(0);
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


    private String splitKey(String businessKey,String beanName){
        String id = "";
        if (businessKey.startsWith(beanName)){
            if (StringUtils.isNotBlank(businessKey)) {
                id = businessKey.split("\\:")[1].toString();
            }
        }
        return id;
    }

    private String getBusinessKey(Object object){
        String businessKey = "";
        if(object instanceof Commodity){
            Commodity commodity = (Commodity) object;
            if(commodity.getId()!=null) {
                businessKey = Commodity.class.getSimpleName() + ":" + commodity.getId();
            }
        }else if(object instanceof Recent){
            Recent recent = (Recent) object;
            if(recent.getId()!=null) {
                businessKey = Recent.class.getSimpleName() + ":" + recent.getId();
            }
        }else if(object instanceof Roche) {
            Roche roche = (Roche) object;
            if(roche.getId()!=null){
                businessKey = Roche.class.getSimpleName() + ":" + roche.getId();
            }

        }
        return businessKey;
    }



}
