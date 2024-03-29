package com.neefull.fsp.web.qff.listener;

import com.neefull.fsp.web.qff.config.SpringBeanUtil;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.system.entity.User;
import com.neefull.fsp.web.system.service.IUserService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

import java.util.List;

/**康德乐人员监听器
 * @Author: chengchengchu
 * @Date: 2019/12/21  18:09
 */
public class CardinalListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        //将人员放入流程中
        IUserService userService = SpringBeanUtil.getObject(IUserService.class);
        List<User> users = userService.findUserByRoleId(87);
        for (User user : users) {
            if(user.getStatus().equals(String.valueOf(ProcessConstant.NEW_BUILD))){
                delegateTask.addCandidateUser(user.getUsername());
            }
        }
    }
}
