package com.neefull.fsp.web.qff.service;

import com.neefull.fsp.web.qff.entity.*;
import com.neefull.fsp.web.system.entity.User;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import java.util.List;
import java.util.Map;

/**
 * @Author: chengchengchu
 * @Date: 2020/1/2  14:53
 */
public interface IProcessService  {

    /**提交流程
     * @param object
     * @param user
     */
    void commitProcess(Object object, User user);

    void  startProcess(Commodity commodity);

    /**找出能审核的人的集合
     * @param object
     * @return
     */
    List<String> getGroupId(Object object);

    /**同意当前任务
     * @param object
     * @param user
     */
//    void agreeCurrentProcess(Object object, User user,String firstCommit);

    /**查询任务的执行流程
     * @param object
     * @return
     */
    Map<String,ProcessHistory> queryHistory(Object object);

    /**查询当前需要完成的任务
     * @param name
     * @return
     */
    List<String> findTask(String name);

    /**删除流程
     * @param object
     */
    void deleteInstance(Object object);

    /**按照key查询流程是否存在
     * @param object
     * @return
     */
    Boolean queryProcessByKey(Object object);

    /**增加修改权限
     * @param user
     */
    void addProcessCommit(User user);

    List<Commodity>  queryCommodityTaskByName(List<Commodity> records, User user);

    List<Recent> queryRecentTaskByName(List<Recent> records, User user);

    List<Roche> queryRocheTaskByName(List<Roche> records, User user);


    /**删除用户
     * @param userIds
     */
    void deleteProcessCommit(String[] userIds);

    List<String> findPrcessName(String username);

    void alterCommodity(Commodity commodity, User currentUser);

    void alterRecent(Recent recent, User currentUser);

    void alterRoche(Roche roche, User currentUser);

}
