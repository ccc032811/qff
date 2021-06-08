package com.neefull.fsp.web.qff.listener;

import com.neefull.fsp.web.qff.config.SpringBeanUtil;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.system.entity.User;
import com.neefull.fsp.web.system.service.IUserService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

import java.util.List;

/**罗氏仓库人员信息
 * @Author: chengchengchu
 * @Date: 2021/4/14  10:10
 */

public class RocheEntreListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        IUserService userService = SpringBeanUtil.getObject(IUserService.class);
        List<User> users = userService.findUserByRoleId(98);
        for (User user : users) {
            if (user.getStatus().equals(String.valueOf(ProcessConstant.NEW_BUILD))) {
                delegateTask.addCandidateUser(user.getUsername());
            }
        }
    }

}
