package com.neefull.fsp.web.qff.listener;

import com.neefull.fsp.web.qff.config.SpringBeanUtil;
import com.neefull.fsp.web.system.entity.User;
import com.neefull.fsp.web.system.service.IUserService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

import java.util.List;

/**罗氏人员监听器
 * @Author: chengchengchu
 * @Date: 2019/12/21  18:01
 */
public class RocheListener implements TaskListener {


    @Override
    public void notify(DelegateTask delegateTask) {
        IUserService userService = SpringBeanUtil.getObject(IUserService.class);
        List<User> users = userService.findUserByRoleId(86);
        for (User user : users) {
            delegateTask.addCandidateUser(user.getUsername());
        }
    }
}
