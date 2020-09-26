package com.neefull.fsp.web.system.controller;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.neefull.fsp.common.util.EncryptUtil;
import com.neefull.fsp.web.common.annotation.Log;
import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.common.entity.FebsConstant;
import com.neefull.fsp.web.common.entity.FebsResponse;
import com.neefull.fsp.web.common.entity.QueryRequest;
import com.neefull.fsp.web.common.exception.FebsException;
import com.neefull.fsp.web.system.entity.User;
import com.neefull.fsp.web.system.service.IUserService;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * @author pei.wang
 */
@Slf4j
@Validated
@RestController
@RequestMapping("sysUser")
public class SysUserController extends BaseController {

    @Autowired
    private IUserService userService;

    @GetMapping("{username}")
    public User getUser(@NotBlank(message = "{required}") @PathVariable String username) {
        return this.userService.findSysUserDetail(username);
    }

    @GetMapping("check/{username}")
    public boolean checkUserName(@NotBlank(message = "{required}") @PathVariable String username, String userId) {
        return this.userService.findByName(username) == null || StringUtils.isNotBlank(userId);
    }

    @GetMapping("list")
    @RequiresPermissions("sysuser:view")
    public FebsResponse userList(User user, QueryRequest request) {
        Map<String, Object> dataTable = getDataTable(this.userService.findSysUserDetail(user, request));
        return new FebsResponse().success().data(dataTable);
    }

    @Log("新增用户")
    @PostMapping
    @RequiresPermissions("sysuser:add")
    public FebsResponse addUser(@Valid User user) throws FebsException {
        try {
            this.userService.createUser(user);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "新增用户失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }

    @Log("删除用户")
    @GetMapping("delete/{userIds}")
    @RequiresPermissions("sysuser:delete")
    public FebsResponse deleteUsers(@NotBlank(message = "{required}") @PathVariable String userIds) throws FebsException {
        try {
            String[] ids = userIds.split(StringPool.COMMA);
            this.userService.deleteUsers(ids);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "删除用户失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }

    @Log("修改用户")
    @PostMapping("update")
    @RequiresPermissions("sysuser:update")
    public FebsResponse updateUser(@Valid User user) throws FebsException {
        try {
            if (user.getUserId() == null)
                throw new FebsException("用户ID为空");
            this.userService.updateUser(user);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "修改用户失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }

    @PostMapping("password/reset/{usernames}")
    @RequiresPermissions("sysuser:password:reset")
    public FebsResponse resetPassword(@NotBlank(message = "{required}") @PathVariable String usernames) throws FebsException {
        try {
            String[] usernameArr = usernames.split(StringPool.COMMA);
            this.userService.resetPassword(usernameArr);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "重置用户密码失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }

    @PostMapping("password/update")
    public FebsResponse updatePassword(
            @NotBlank(message = "{required}") String oldPassword,
            @NotBlank(message = "{required}") String newPassword) throws FebsException {
        try {
            User user = getCurrentUser();
            if (!StringUtils.equals(user.getPassword(), EncryptUtil.encrypt(oldPassword, FebsConstant.AES_KEY))) {
                throw new FebsException("原密码不正确");
            }
            userService.updatePassword(user.getUsername(), newPassword);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "修改密码失败，" + e.getMessage();
            log.error(message, e);
            throw new FebsException(message);
        }
    }

    @GetMapping("avatar/{image}")
    public FebsResponse updateAvatar(@NotBlank(message = "{required}") @PathVariable String image) throws FebsException {
        try {
            User user = getCurrentUser();
            this.userService.updateAvatar(user.getUsername(), image);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "修改头像失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }

    @PostMapping("theme/update")
    public FebsResponse updateTheme(String theme, String isTab) throws FebsException {
        try {
            User user = getCurrentUser();
            this.userService.updateTheme(user.getUsername(), theme, isTab);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "修改系统配置失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }

    @PostMapping("profile/update")
    public FebsResponse updateProfile(User user) throws FebsException {
        try {
            User currentUser = getCurrentUser();
            user.setUserId(currentUser.getUserId());
            this.userService.updateProfile(user);
            return new FebsResponse().success();
        } catch (Exception e) {
            String message = "修改个人信息失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }

    @GetMapping("excel")
    @RequiresPermissions("sysuser:export")
    public void export(QueryRequest queryRequest, User user, HttpServletResponse response) throws FebsException {
        try {
            List<User> users = this.userService.findSysUserDetail(user, queryRequest).getRecords();
            ExcelKit.$Export(User.class, response).downXlsx(users, false);
        } catch (Exception e) {
            String message = "导出Excel失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }
}
