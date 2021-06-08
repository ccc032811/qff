package com.neefull.fsp.web.qff.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.neefull.fsp.web.common.exception.FebsException;
import com.neefull.fsp.web.qff.config.ProcessInstanceProperties;
import com.neefull.fsp.web.qff.config.SendMailProperties;
import com.neefull.fsp.web.qff.entity.*;
import com.neefull.fsp.web.qff.service.*;
import com.neefull.fsp.web.qff.utils.MailUtils;
import com.neefull.fsp.web.qff.utils.PageUtils;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.system.entity.Role;
import com.neefull.fsp.web.system.entity.User;
import com.neefull.fsp.web.system.service.IUserService;
import com.neefull.fsp.web.system.service.impl.RoleServiceImpl;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.validation.constraints.NotBlank;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author: chengchengchu
 * @Date: 2020/1/2  14:55
 */

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class ProcessServiceImpl implements IProcessService {


    private final static String PROSTYPE = "processType";


    @Autowired
    private ICommodityService commodityService;
    @Autowired
    private IAttachmentService attachmentService;
    @Autowired
    private IRecentService recentService;
    @Autowired
    private IRocheService rocheService;
    @Autowired
    private RepositoryService repositoryService;
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
    private RedisTemplate redisTemplate;


    /**
     * 进行流程提交
     * @param object
     * @param user
     */
    @Override
    @Transactional
    public void commitProcess(Object object, User user) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Attachment> attachments = new ArrayList<>();

        if(object instanceof Commodity){
            Commodity commodity = (Commodity) object;
            String businessKey = getBusinessKey(commodity);

            //对于手工新入库的数据
            if(commodity.getId()==null){
                //获取最新数据最新时间
                String lastDate = commodityService.selectLastTime();
                Date parse = null;
                try {
                    parse = simpleDateFormat.parse(lastDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                commodity.setCreateTime(parse);
                //入库
                commodityService.addCommodity(commodity);
                //启动流程
                startProcess(commodity);

                if(StringUtils.isNotEmpty(commodity.getImages())){
                    //添加附件
                    attachments = addOrEditFiles(commodity, user);
                }
                //其他手工QFF 的时候需要发邮件
                if(commodity.getStage().equals(ProcessConstant.WRAPPER_NAME)){
                    Map<String, String> files = new HashMap<>();
                    for (Attachment attachment : attachments) {
                        files.put(attachment.getRemark()+ StringPool.DOT + attachment.getAttachType(),
                                properties.getImagePath() + attachment.getRemark() + StringPool.DOT + attachment.getAttachType());
                    }
                    String[] rocheMails = MailUtils.getEmails(86);

                    List<Commodity> commodityList = new ArrayList<>();
                    commodityList.add(commodity);

                    Context context = new Context();
                    context.setVariable("list", commodityList);
                    String text = templateEngine.process("rocheOtherCommodity", context);
                    MailUtils.sendMail(commodity.getStage(),text, mailProperties, rocheMails, files);
                }
            //对于sap过来的数据
            }else {
                editCommodity(commodity);
                if(StringUtils.isNotEmpty(commodity.getImages())){
                    attachments = addOrEditFiles(commodity, user);
                }
                //同意当前流程
                agreeProcess(businessKey, user, attachments);

                if(queryProcessInstance(businessKey)==null){
                    //判断流程中是否有这条数据，没有就将这条QFF状态修改为审核完成
                    commodityService.updateCommodityStatus(commodity.getId(),ProcessConstant.HAS_FINISHED);
                }
            }

        }else if(object instanceof Recent){
            Recent recent = (Recent) object;

            Map<String,String> files = new HashMap<>();
            String businessKey = getBusinessKey(recent);

            //手动入库的数据
            if(recent.getId()==null){
                recent.setStartDate(DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
                //数据，附件信息入库
                recentService.addRecent(recent);

                if(StringUtils.isNotEmpty(recent.getImages())){
                    attachments = addOrEditFiles(recent, user);
                    for (Attachment attachment : attachments) {
                        files.put(attachment.getRemark()+ StringPool.DOT + attachment.getAttachType(),
                                properties.getImagePath() + attachment.getRemark() + StringPool.DOT + attachment.getAttachType());
                    }
                }

                businessKey = getBusinessKey(recent);
                //启动流程
                startProcess(properties.getRecentProcess(),businessKey,recent.getStage());
                //第一步自动提交
                Task task = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).singleResult();
                taskService.setAssignee(task.getId(),"康德乐发起申请");
                taskService.complete(task.getId());
                //更改状态
                recentService.updateRecentStatus(recent.getId(), ProcessConstant.UNDER_REVIEW);
                //发邮件
                String[] rocheMails = MailUtils.getEmails(86);
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

                MailUtils.sendMail(recent.getStage(),text,mailProperties,rocheMails,files);
            }else {
                editCommodity(recent);

                if(StringUtils.isNotEmpty(recent.getImages())){
                    attachments = addOrEditFiles(recent, user);
                }
                agreeProcess(businessKey, user, attachments);

                if(queryProcessInstance(businessKey)==null){
                    recentService.updateRecentStatus(recent.getId(),ProcessConstant.HAS_FINISHED);
                }
            }

        }else if(object instanceof Roche){
            Roche roche = (Roche) object;
            String businessKey = getBusinessKey(roche);
            //手动入库的数据
            if(roche.getId()==null){
                //数据，附件信息入库
                rocheService.addRoche(roche);

                if(StringUtils.isNotEmpty(roche.getImages())){
                    addOrEditFiles(roche, user);
                }
                businessKey = getBusinessKey(roche);
                startProcess(properties.getRocheProcess(),businessKey,"roche");

                Task task = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).singleResult();
                taskService.setAssignee(task.getId(),"罗氏发起申请");
                taskService.complete(task.getId());

                rocheService.updateRocheStatus(roche.getId(), ProcessConstant.UNDER_REVIEW);
            }else {
                boolean b = agreeProcess(businessKey, user, attachments);
                if(b){
                    rocheService.editRoche(roche);

                    if(StringUtils.isNotEmpty(roche.getImages())){
                        attachments = addOrEditFiles(roche, user);
                    }
                }
                //对罗氏内部发起最终审核完成，发送邮件到两方
                if(queryProcessInstance(businessKey)==null){
                    rocheService.updateRocheStatus(roche.getId(),ProcessConstant.HAS_FINISHED);
                    // 发送邮件
                    Map<String,String> files = new HashMap<>();
                    String[] rocheMails = MailUtils.getEmails(86);
                    String[] kdlMails = MailUtils.getEmails(87);
                    String[] mails = new String[rocheMails.length+kdlMails.length];
                    System.arraycopy(rocheMails,0,mails,0,rocheMails.length);
                    System.arraycopy(kdlMails,0,mails,rocheMails.length,kdlMails.length);

                    List<Roche> rocheList =new ArrayList<>();
                    rocheList.add(roche);

                    Context context = new Context();
                    context.setVariable("list",rocheList);
                    String text = templateEngine.process("rocheRoche", context);
                    MailUtils.sendMail(ProcessConstant.ROCHE_NAME,text,mailProperties,mails,files);

                }
            }
        }
    }



    //启动流程
    @Transactional
    @Override
    public void startProcess(Commodity commodity){
        String businessKey = getBusinessKey(commodity);
        //启动流程
        startProcess(properties.getCommodityProcess(),businessKey,commodity.getStage());
        //第一次自动提交
        Task task = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).singleResult();
        taskService.setAssignee(task.getId(),"康德乐发起申请");
        taskService.complete(task.getId());
        //更改状态
        commodityService.updateCommodityStatus(commodity.getId(), ProcessConstant.UNDER_REVIEW);
    }

    //审批流程
    @Transactional
    protected boolean agreeProcess(String businessKey,User user,List<Attachment> attachments){
        //同意流程

//        if(redisTemplate.hasKey(businessKey)){
//            return false;
//        }else {
//            redisTemplate.opsForValue().set(businessKey,businessKey, 60,TimeUnit.SECONDS);
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
            return true;
//        }
    }


    @Transactional
    protected void editCommodity(Object object){
        //这个是更改审核时间
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
    protected void startProcess(String definitionKey,String businessKey,String type){
        //启动流程，并将QFF类型放入到流程中
        Map<String,Object> map = new HashMap<>();
        if(type.equals(ProcessConstant.DELIVERY_NAME)){
            map.put(PROSTYPE,"delivery");
        }else if(type.equals(ProcessConstant.CONSERVE_NAME)){
            map.put(PROSTYPE,"conserve");
        }else if(type.equals(ProcessConstant.WRAPPER_NAME)){
            map.put(PROSTYPE,"wrapper");
        }else if(type.equals(ProcessConstant.REFUND_NAME)){
            map.put(PROSTYPE,"refund");
        } else if(type.equals(ProcessConstant.RECENT_NAME)){
            map.put(PROSTYPE,"recent");
        } else if(type.equals(ProcessConstant.TEMPERATURE_NAME)){
            map.put(PROSTYPE,"temperature");
        } else if(type.equals(ProcessConstant.STORE_PACKAGE_EXPORT)) {
            map.put(PROSTYPE, "conserve");
        } else {
            map.put(PROSTYPE,type);
        }
        runtimeService.startProcessInstanceByKey(definitionKey,businessKey,map);

    }


    @Override
    public List<String> getGroupId(Object object) {
        //这个是查询这条QFF 某个 节点可审核人的集合
        List<String> list = new ArrayList<>();
        String businessKey = getBusinessKey(object);

        if(StringUtils.isNotEmpty(businessKey)){
            List<Task> taskList = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).list();
            if(CollectionUtils.isNotEmpty(taskList)){
                for (Task task : taskList) {
                    List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask(task.getId());
                    for (IdentityLink identityLink : identityLinksForTask) {
                        list.add(identityLink.getUserId());
                    }
                }
            }
        }
        return list;
    }


    @Override
    public  Map<String,ProcessHistory> queryHistory(Object object) {
        //查询流程的审批过程
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
    public List<String> findTask(User user) {
        //查询当前人要审核的数据
        List<Task> list = queryTaskByUserName(user.getUsername());
        List<Commodity> commodityList = commodityService.queryProcessList(ProcessConstant.UNDER_REVIEW);
        List<Recent> recentList = recentService.queryProcessList(ProcessConstant.UNDER_REVIEW);
        List<String> names = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(list)) {
            for (Task task : list) {
//                String type = (String) taskService.getVariable(task.getId(), "processType");
                ProcessInstance processInstance = getProcessInstanceById(task.getProcessInstanceId());
                String type = (String)  runtimeService.getVariable(processInstance.getProcessInstanceId(), "processType");
                if(StringUtils.isNotEmpty(type)) {
                    names.add(type);
                }else {
//                    ProcessInstance processInstance = getProcessInstanceById(task.getProcessInstanceId());
                    String businessKey = processInstance.getBusinessKey();
                    if (businessKey.startsWith("Commodity")) {
                        String id = splitKey(businessKey, "Commodity");
                        for (Commodity commodity : commodityList) {
                            if(commodity.getId() == Integer.parseInt(id)){
                                names.add(choseProcessType(commodity));
                            }
                        }
                    }else if(businessKey.startsWith("Recent")) {
                        String id = splitKey(businessKey, "Recent");
                        for (Recent recent : recentList) {
                            if(recent.getId() == Integer.parseInt(id)){
                                names.add(choseProcessType(recent));
                            }
                        }
                    }else if(businessKey.startsWith("Roche")){
                        names.add("roche");
                    }
                }
            }
        }

//        if(CollectionUtils.isNotEmpty(list)){
//            //这个是查询具体的流程的种类，并放到集合中
//            for (Task task : list) {
//                ProcessInstance processInstance = getProcessInstanceById(task.getProcessInstanceId());
//                String type = (String) runtimeService.getVariable(processInstance.getProcessInstanceId(), "processType");
//
//                String activityId = processInstance.getActivityId();
//                if(StringUtils.isNotEmpty(type)){
//                    if(("delivery").equals(type)||("conserve").equals(type)||("wrapper").equals(type)||("refund").equals(type)){
//                        if(roleId.contains("87")){
//                            if(!activityId.equals(ProcessConstant.THREE_STEP)) {
//                                names.add(type);
//                            }
//                        }else {
//                            if(roleId.contains("98")&&!activityId.equals(ProcessConstant.THREE_STEP)){
//                                names.add(type);
//                            }else if(!roleId.contains("98")&&activityId.equals(ProcessConstant.THREE_STEP)){
//                                names.add(type);
//                            }
//                        }
//                    }else if(("recent").equals(type)||("temperature").equals(type)){
//                        if(roleId.contains("87")){
//                            if(!activityId.equals(ProcessConstant.THREE_STEP)) {
//                                names.add(type);
//                            }
//                        }else {
//                            if(roleId.contains("98")&&!activityId.equals(ProcessConstant.THREE_STEP)){
//                                names.add(type);
//                            }else if(!roleId.contains("98")&&activityId.equals(ProcessConstant.THREE_STEP)){
//                                names.add(type);
//                            }
//                        }
//                    }else if(("roche").equals(type)){
//                        if(roleId.contains("98")){
//                            names.add("roche");
//                        }
//                    }
//                }else {
//                    if(processInstance.getBusinessKey().startsWith("Recent")){
//                        String id = processInstance.getBusinessKey().split("\\:")[1];
//                        Recent recent = recentService.queryRecentById(Integer.parseInt(id));
//                        if(roleId.contains("87")){
//                            if(!activityId.equals(ProcessConstant.THREE_STEP)) {
//                                names.add(choseProcessType(recent));
//                            }
//                        }else {
//                            if(roleId.contains("98")&&!activityId.equals(ProcessConstant.THREE_STEP)){
//                                names.add(choseProcessType(recent));
//                            }else if(!roleId.contains("98")&&activityId.equals(ProcessConstant.THREE_STEP)){
//                                names.add(choseProcessType(recent));
//                            }
//                        }
//                    }else if(processInstance.getBusinessKey().startsWith("Roche")) {
//                        if(roleId.contains("98")){
//                            names.add("roche");
//                        }
//                    }else {
//                        String id = processInstance.getBusinessKey().split("\\:")[1];
//                        Commodity commodity = commodityService.queryCommodityById(Integer.parseInt(id));
//                        if(roleId.contains("87")){
//                            if(!activityId.equals(ProcessConstant.THREE_STEP)) {
//                                names.add(choseProcessType(commodity));
//                            }
//                        }else {
//                            if(roleId.contains("98")&&!activityId.equals(ProcessConstant.THREE_STEP)){
//                                names.add(choseProcessType(commodity));
//                            }else if(!roleId.contains("98")&&activityId.equals(ProcessConstant.THREE_STEP)){
//                                names.add(choseProcessType(commodity));
//                            }
//                        }
//                    }
//                }
//            }


        return names;
    }


    @Override
    public List<Commodity> queryCommodityTaskByName(List<Commodity> commodityList, User user,Integer att) {
        //查询这个用户可审核的数据
        List<Task> tasks = queryTaskByUserName(user.getUsername());
        List<String> roleIds = Arrays.asList(user.getRoleId().split(","));

        if(att !=null&&roleIds.contains("98")&&att == 1){
            for (Task task : tasks) {
                ProcessInstance processInstance = getProcessInstanceById(task.getProcessInstanceId());
                List<Task> list = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).list();
                String id = splitKey(processInstance.getBusinessKey(), Commodity.class.getSimpleName());
                if(StringUtils.isNotEmpty(id)){
                    for (Commodity commodity : commodityList) {
                        if(commodity.getId()==Integer.parseInt(id)&&commodity.getStatus()==2) {
                            if(commodity.getStage().equals(ProcessConstant.REFUND_NAME)){
                                commodity.setIsAllow(1);
                            }else if(!commodity.getStage().equals(ProcessConstant.REFUND_NAME)&&list.size()==1){
                                commodity.setIsAllow(1);
                            }
                        }
                    }
                }
            }
        }else {
            for (Task task : tasks) {
                ProcessInstance processInstance = getProcessInstanceById(task.getProcessInstanceId());
                String id = splitKey(processInstance.getBusinessKey(), Commodity.class.getSimpleName());
                if(StringUtils.isNotEmpty(id)){
                    for (Commodity commodity : commodityList) {
                        if(commodity.getId()==Integer.parseInt(id)&&commodity.getStatus()==2) {
                            commodity.setIsAllow(1);
                        }
                    }
                }
            }
        }
        return commodityList;
    }


    @Override
    public List<Recent> queryRecentTaskByName(List<Recent> recentList, User user) {
        //查询这个用户可审核的数据
        List<Task> tasks = queryTaskByUserName(user.getUsername());
        for (Task task : tasks) {
            ProcessInstance processInstance = getProcessInstanceById(task.getProcessInstanceId());
            String id = splitKey(processInstance.getBusinessKey(), Recent.class.getSimpleName());
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
        //查询这个用户可审核的数据
        List<Task> tasks = queryTaskByUserName(user.getUsername());
        for (Task task : tasks) {
            ProcessInstance processInstance = getProcessInstanceById(task.getProcessInstanceId());
            String id = splitKey(processInstance.getBusinessKey(), Roche.class.getSimpleName());
            if(StringUtils.isNotEmpty(id)){
                for (Roche roche : rocheList) {
                    roche.setIsAllow(1);
                }
            }
        }
        return rocheList;
    }


    private String choseProcessType(Object object){
        String type = "";
        if(object instanceof Commodity){
            Commodity commodity = (Commodity) object;
            if(commodity.getStage().equals(ProcessConstant.DELIVERY_NAME)){
                type = "delivery";
            }else if(commodity.getStage().equals(ProcessConstant.CONSERVE_NAME)){
                type = "conserve";
            } else if(commodity.getStage().equals(ProcessConstant.STORE_PACKAGE_EXPORT)){
                type = "conserve";
            }else if(commodity.getStage().equals(ProcessConstant.WRAPPER_NAME)){
                type = "wrapper";
            }else if(commodity.getStage().equals(ProcessConstant.REFUND_NAME)){
                type = "refund";
            }
        }else if(object instanceof Recent){
            Recent recent = (Recent) object;
            if(recent.getStage().equals(ProcessConstant.RECENT_NAME)){
                type = "recent";
            }else if(recent.getStage().equals(ProcessConstant.TEMPERATURE_NAME)){
                type = "temperature";
            }
        }
        return type;
    }


    @Override
    public List<String> findPrcessName(User user) {
        //查询具体可审核的类型数量
        List<String> stringList = findTask(user);
        List<String> menuName = new ArrayList<>();
        if(stringList.contains("recent")){
            menuName.add(ProcessConstant.RECENT_NAME);
        }
        if(stringList.contains("temperature")){
            menuName.add(ProcessConstant.TEMPERATURE_NAME);
        }
        if(stringList.contains("roche")){
            menuName.add(ProcessConstant.ROCHE_NAME);
        }
        if(stringList.contains("delivery")){
            menuName.add(ProcessConstant.DELIVERY_NAME);
        }
        if(stringList.contains("conserve")){
            menuName.add(ProcessConstant.STORE_PACKAGE_EXPORT);
        }
        if(stringList.contains("wrapper")){
            menuName.add(ProcessConstant.WRAPPER_NAME);
        }
        if(stringList.contains("refund")){
            menuName.add(ProcessConstant.REFUND_NAME);
        }
        return menuName;
    }


    private List<Task> queryTaskByUserName(String name){
        return taskService.createTaskQuery().taskCandidateUser(name).list();
    }


    @Override
    @Transactional
    public void deleteInstance(Object object) {
        //删除流程
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
    public Boolean queryProcessByKey(Commodity commodity) {
        //查询流程
        ProcessInstance processInstance = null;
        String businessKey =  Commodity.class.getSimpleName()+":"+commodity.getId();
        processInstance = queryProcessInstance(businessKey);
        if(processInstance == null){
            return false;
        }
        return true;

    }

    @Transactional
    public List<Task> deleteProcessUser(User user){
        //将流程中有这个审核的人的信息删除
        List<Task> list = taskService.createTaskQuery().taskCandidateUser(user.getUsername()).list();
        if(CollectionUtils.isNotEmpty(list)){
            for (Task task : list) {
                taskService.deleteCandidateUser(task.getId(),user.getUsername());
            }
        }
        return taskService.createTaskQuery().list();
    }



    @Override
    @Transactional
    public void addProcessCommit(User user) {
        //将用户添加到相应的流程中
        List<Task> list = deleteProcessUser(user);
        String roleId = user.getRoleId();
        if(CollectionUtils.isNotEmpty(list)){
            for (Task task : list) {
                ProcessInstance processInstance = getProcessInstanceById(task.getProcessInstanceId());
                String processDefinitionKey = processInstance.getProcessDefinitionKey();
                if(processDefinitionKey.equals(properties.getCommodityProcess())){
                    if(roleId.contains("87")&&task.getTaskDefinitionKey().equals(ProcessConstant.FOUR_STEP)){
                        taskService.addCandidateUser(task.getId(), user.getUsername());
                    }else if(roleId.contains("86")&&task.getTaskDefinitionKey().equals(ProcessConstant.THREE_STEP)){
                        taskService.addCandidateUser(task.getId(), user.getUsername());
                    }else if(roleId.contains("98")&&task.getTaskDefinitionKey().equals(ProcessConstant.TWELVE_STEP)){
                        taskService.addCandidateUser(task.getId(), user.getUsername());
                    }
                }else if(processDefinitionKey.equals(properties.getRecentProcess())){
                    if(roleId.contains("87")&&task.getTaskDefinitionKey().equals(ProcessConstant.FOUR_STEP)){
                        taskService.addCandidateUser(task.getId(), user.getUsername());
                    }else if(roleId.contains("86")&&task.getTaskDefinitionKey().equals(ProcessConstant.THREE_STEP)){
                        taskService.addCandidateUser(task.getId(), user.getUsername());
                    }else if(roleId.contains("98")&&task.getTaskDefinitionKey().equals(ProcessConstant.EIGHT_STEP)){
                        taskService.addCandidateUser(task.getId(), user.getUsername());
                    }
                }else if(processDefinitionKey.equals(properties.getRocheProcess())){
                    if(roleId.contains("87")&&task.getTaskDefinitionKey().equals(ProcessConstant.THREE_STEP)){
                        taskService.addCandidateUser(task.getId(), user.getUsername());
                    }else if(roleId.contains("98")&&task.getTaskDefinitionKey().equals(ProcessConstant.SIX_STEP)){
                        taskService.addCandidateUser(task.getId(), user.getUsername());
                    }
                }
            }


//            if(roleId.contains("87")){
//                for (Task task : list) {
//                    ProcessInstance processInstance = getProcessInstanceById(task.getProcessInstanceId());
//                    String processDefinitionKey = processInstance.getProcessDefinitionKey();
//                    if(processDefinitionKey.equals(properties.getCommodityProcess())
//                            &&task.getTaskDefinitionKey().equals(ProcessConstant.FOUR_STEP)){
//                        taskService.addCandidateUser(task.getId(), user.getUsername());
//
//                    }else if(processDefinitionKey.equals(properties.getRecentProcess())
//                            &&task.getTaskDefinitionKey().equals(ProcessConstant.FOUR_STEP)){
//                        taskService.addCandidateUser(task.getId(),user.getUsername());
//                    }else if(processDefinitionKey.equals(properties.getRocheProcess())
//                            &&task.getTaskDefinitionKey().equals(ProcessConstant.THREE_STEP)){
//                        taskService.addCandidateUser(task.getId(),user.getUsername());
//                    }
//                }
//
//            }else if(roleId.contains("86")){
//                for (Task task : list) {
//                    ProcessInstance processInstance = getProcessInstanceById(task.getProcessInstanceId());
//                    String processDefinitionKey = processInstance.getProcessDefinitionKey();
//                    if(processDefinitionKey.equals(properties.getCommodityProcess())) {
//                        if (task.getTaskDefinitionKey().equals(ProcessConstant.THREE_STEP)
//                                ||task.getTaskDefinitionKey().equals(ProcessConstant.TWELVE_STEP)){
//                            taskService.addCandidateUser(task.getId(), user.getUsername());
//                        }
//                    }else if(processDefinitionKey.equals(properties.getRecentProcess())) {
//                        if(task.getTaskDefinitionKey().equals(ProcessConstant.THREE_STEP)
//                                ||task.getTaskDefinitionKey().equals(ProcessConstant.EIGHT_STEP)){
//                            taskService.addCandidateUser(task.getId(), user.getUsername());
//                        }
//                    }else if(task.getTaskDefinitionKey().equals(ProcessConstant.SIX_STEP)
//                            &&processDefinitionKey.equals(properties.getRocheProcess())){
//                        taskService.addCandidateUser(task.getId(),user.getUsername());
//                    }
//                }
//            }
        }

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
    @Transactional
    public void alterCommodity(Commodity commodity, User currentUser) {
        //变更信息
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
        //有附件就添加
        if(StringUtils.isNotEmpty(commodity.getImages())){
            addOrEditFiles(commodity,currentUser);
        }

        List<Task> taskList = queryTask(getBusinessKey(commodity));
        //发送邮件
        if(taskList.size()==1){
            String[] activityIds = new String[]{"_3","_4","_12"};
            rollbackPrcoess("_3",getBusinessKey(commodity),currentUser.getUsername(),taskList.get(0),activityIds);
        }else {
            String[] mails = MailUtils.getEmails(87);

            List<Commodity> list =new ArrayList<>();
            list.add(commodity);
            Map<String,String> files = new HashMap<>();

            Context context = new Context();
            context.setVariable("list",list);
            String text = "";
            if(commodity.getStage().equals(ProcessConstant.WRAPPER_NAME)){
                text= templateEngine.process("kdlOtherCommodity", context);
                MailUtils.sendMail(commodity.getStage(),text,mailProperties,mails,files);
            }else {
                text= templateEngine.process("kdlCommodity", context);
                if(commodity.getStage().equals(ProcessConstant.CONSERVE_NAME)){
                    MailUtils.sendMail(ProcessConstant.STORE_PACKAGE_EXPORT,text,mailProperties,mails,files);
                }else {
                    MailUtils.sendMail(commodity.getStage(),text,mailProperties,mails,files);
                }

            }

        }
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
        if(!recent.getComment().equals(oldRecent.getComment())){
            alteration.append("备注:" +date+ " 由 "+oldRecent.getComment()+" 修改为 "+recent.getComment()+"  。 ");
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

        List<Task> taskList = queryTask(getBusinessKey(recent));
        if(taskList.size()==1){
            String[] activityIds = new String[]{"_3","_4","_8"};
            rollbackPrcoess("_3",getBusinessKey(recent),currentUser.getUsername(),taskList.get(0),activityIds);
        }else {
            String[] mails = MailUtils.getEmails(87);
            List<Recent> list =new ArrayList<>();
            list.add(recent);

            Context context = new Context();
            context.setVariable("list",list);
            String text = "";
            if(recent.getStage().equals(ProcessConstant.RECENT_NAME)){
                text = templateEngine.process("kdlRecent", context);
                MailUtils.sendMail(recent.getStage(),text,mailProperties,mails,files);

            }else if(recent.getStage().equals(ProcessConstant.TEMPERATURE_NAME)){
                text = templateEngine.process("kdlTemperature", context);
                MailUtils.sendMail(recent.getStage(),text,mailProperties,mails,files);
            }
        }
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

        List<Task> taskList = queryTask(getBusinessKey(roche));

        if(taskList.size()==1){
            String[] activityIds = new String[]{"_3","_5","_6"};
            rollbackPrcoess("_5",getBusinessKey(roche),currentUser.getUsername(),taskList.get(0),activityIds);
        }else {
            String[] mails = MailUtils.getEmails(87);

            List<Roche> list =new ArrayList<>();
            list.add(roche);
            Map<String,String> files = new HashMap<>();

            Context context = new Context();
            context.setVariable("list",list);
            String text = templateEngine.process("kdlRoche", context);
            MailUtils.sendMail(ProcessConstant.ROCHE_NAME,text,mailProperties,mails,files);
        }
    }


    private void rollbackPrcoess(String destTaskKey,String businessKey,String userName,Task task,String[] activityIds){

        ProcessInstance processInstance = queryProcessInstance(businessKey);

        try {
            List<HistoricActivityInstance> userTask = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getProcessInstanceId()).activityType("userTask").list();
            for (HistoricActivityInstance historicActivityInstance : userTask) {
                for (String activityId : activityIds) {
                    if(historicActivityInstance.getActivityId().equals(activityId)){
                        historyService.deleteHistoricTaskInstance(historicActivityInstance.getTaskId());
                    }
                }
            }
            ActivityImpl activitiImpl =  findActivitiImpl(task.getId(),null);
            List<PvmTransition> oriPvmTransitionList = clearTransition(activitiImpl);

            TransitionImpl newTransition = activitiImpl.createOutgoingTransition();

            ActivityImpl pointActivity = findActivitiImpl(task.getId(), destTaskKey);

            newTransition.setDestination(pointActivity);

            taskService.complete(task.getId());

            pointActivity.getIncomingTransitions().remove(newTransition);
            restoreTransition(activitiImpl,oriPvmTransitionList);

            List<Execution> list = runtimeService.createExecutionQuery().parentId(processInstance.getProcessInstanceId()).list();
            for (Execution execution : list) {
                if(execution.getActivityId().equals("_11")||execution.getActivityId().equals("_12")||execution.getActivityId().equals("_13")){
                    runtimeService.deleteProcessInstance(execution.getId(),"");
                }
            }

            Task newTask = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).singleResult();
            taskService.setAssignee(newTask.getId(),userName);
            taskService.complete(newTask.getId());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private ActivityImpl findActivitiImpl(String taskId, String activityId) throws Exception {
        ProcessDefinitionEntity processDefinition = findProcessDefinitionEntityByTaskId(taskId);

        if (StringUtils.isEmpty(activityId)) {
            activityId = findTaskById(taskId).getTaskDefinitionKey();
        }
        if (activityId.toUpperCase().equals("END")) {
            for (ActivityImpl activityImpl : processDefinition.getActivities()) {
                List<PvmTransition> pvmTransitionList = activityImpl
                        .getOutgoingTransitions();
                if (pvmTransitionList.isEmpty()) {
                    return activityImpl;
                }
            }
        }
        return ((ProcessDefinitionImpl) processDefinition).findActivity(activityId);
    }


    private List<PvmTransition> clearTransition(ActivityImpl activityImpl) {
        List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
        List<PvmTransition> pvmTransitionList = activityImpl
                .getOutgoingTransitions();
        for (PvmTransition pvmTransition : pvmTransitionList) {
            oriPvmTransitionList.add(pvmTransition);
        }
        pvmTransitionList.clear();
        return oriPvmTransitionList;
    }

    private void restoreTransition(ActivityImpl activityImpl, List<PvmTransition> oriPvmTransitionList) {
        List<PvmTransition> pvmTransitionList = activityImpl.getOutgoingTransitions();
        pvmTransitionList.clear();
        for (PvmTransition pvmTransition : oriPvmTransitionList) {
            pvmTransitionList.add(pvmTransition);
        }
    }


    private ProcessDefinitionEntity findProcessDefinitionEntityByTaskId(String taskId) throws Exception {
        return (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(findTaskById(taskId)
                        .getProcessDefinitionId());
    }

    private TaskEntity findTaskById(String taskId)  {
        return (TaskEntity) taskService.createTaskQuery().taskId(
                taskId).singleResult();
    }


    private ProcessInstance getProcessInstanceById(String processInstanceId){
        return  runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    }

    private ProcessInstance queryProcessInstance(String businessKey){
        return runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();

    }

    private List<Task> queryTask(String businessKey){
        return taskService.createTaskQuery().processInstanceBusinessKey(businessKey).list();

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
        //新增附件
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
                attachment.setEnable(1);
                attachment.setVest(user.getUsername());
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
        //获取id  拼接成key
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
