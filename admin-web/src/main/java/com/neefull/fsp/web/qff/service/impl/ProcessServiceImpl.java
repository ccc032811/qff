package com.neefull.fsp.web.qff.service.impl;

import com.neefull.fsp.web.qff.config.ProcessInstanceProperties;
import com.neefull.fsp.web.qff.entity.*;
import com.neefull.fsp.web.qff.service.*;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    public Integer findTask(String name) {
        List<Task> list = queryTaskByUserName(name);
        return list.size();
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

    private ProcessInstance getProcessInstanceById(String processInstanceId){
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


}
