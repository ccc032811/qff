package com.neefull.fsp.web.system.controller;

import com.neefull.fsp.web.common.authentication.ShiroHelper;
import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.common.entity.FebsConstant;
import com.neefull.fsp.web.common.utils.DateUtil;
import com.neefull.fsp.web.common.utils.FebsUtil;
import com.neefull.fsp.web.system.entity.*;
import com.neefull.fsp.web.system.service.IUserService;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.session.ExpiredSessionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @author pei.wang
 */
@Controller("systemView")
public class ViewController extends BaseController {

    @Autowired
    private IUserService userService;
    @Autowired
    private ShiroHelper shiroHelper;



    @GetMapping("login")
    @ResponseBody
    public Object login(HttpServletRequest request) {
        if (FebsUtil.isAjaxRequest(request)) {
            throw new ExpiredSessionException();
        } else {
            ModelAndView mav = new ModelAndView();
            mav.setViewName(FebsUtil.view("login"));
            return mav;
        }
    }

    @GetMapping("unauthorized")
    public String unauthorized() {
        return FebsUtil.view("error/403");
    }


    @GetMapping("/")
    public String redirectIndex() {
        return "redirect:/index";
    }

    @GetMapping("index")
    public String index(Model model) {
        AuthorizationInfo authorizationInfo = shiroHelper.getCurrentuserAuthorizationInfo();
        User user = super.getCurrentUser();
        user.setPassword("It's a secret");
        model.addAttribute("user", userService.findByName(user.getUsername()));
        model.addAttribute("permissions", authorizationInfo.getStringPermissions());
        model.addAttribute("roles", authorizationInfo.getRoles());

        return "index";
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "layout")
    public String layout() {
        return FebsUtil.view("layout");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "password/update")
    public String passwordUpdate() {
        return FebsUtil.view("system/user/passwordUpdate");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "user/profile")
    public String userProfile() {
        return FebsUtil.view("system/user/userProfile");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "user/avatar")
    public String userAvatar() {
        return FebsUtil.view("system/user/avatar");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "user/profile/update")
    public String profileUpdate() {
        return FebsUtil.view("system/user/profileUpdate");
    }


    //**************************************用户管理模块 start *********************************************

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/sysUser")
    @RequiresPermissions("qf:sysuser:view")
    public String sysUser() {
        return FebsUtil.view("system/user/sysuser");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/sysUser/add")
    @RequiresPermissions("qf:sysuser:add")
    public String sysUserAdd() {
        return FebsUtil.view("system/user/sysuserAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/sysUser/detail/{username}")
    @RequiresPermissions("qf:sysuser:view")
    public String sysUserDetail(@PathVariable String username, Model model) {
        resolveSysUserModel(username, model, true);
        return FebsUtil.view("system/user/sysuserDetail");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/sysUser/update/{username}")
    @RequiresPermissions("qf:sysuser:update")
    public String sysUserUpdate(@PathVariable String username, Model model) {
        resolveSysUserModel(username, model, false);
        return FebsUtil.view("system/user/sysuserUpdate");
    }

    //**************************************用户管理模块 end *********************************************

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/role")
    @RequiresPermissions("qf:role:view")
    public String systemRole() {
        return FebsUtil.view("system/role/role");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/menu")
    @RequiresPermissions("qf:menu:view")
    public String systemMenu() {
        return FebsUtil.view("system/menu/menu");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/dept")
    @RequiresPermissions("qf:dept:view")
    public String systemDept() {
        return FebsUtil.view("system/dept/dept");
    }

    @RequestMapping(FebsConstant.VIEW_PREFIX + "index")
    public String pageIndex() {
        return FebsUtil.view("index");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "404")
    public String error404() {
        return FebsUtil.view("error/404");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "403")
    public String error403() {
        return FebsUtil.view("error/403");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "500")
    public String error500() {
        return FebsUtil.view("error/500");
    }

    private void resolveSysUserModel(String username, Model model, Boolean transform) {
        User user = userService.findByName(username);
        model.addAttribute("user", user);
        if (transform) {
            String ssex = user.getSex();
            if (User.SEX_MALE.equals(ssex)) user.setSex("男");
            else if (User.SEX_FEMALE.equals(ssex)) user.setSex("女");
            else user.setSex("保密");
        }
        if (user.getLastLoginTime() != null)
            model.addAttribute("lastLoginTime", DateUtil.getDateFormat(user.getLastLoginTime(), DateUtil.FULL_TIME_SPLIT_PATTERN));
    }


    @GetMapping(FebsConstant.VIEW_PREFIX + "/others/febs/icon")
    @RequiresPermissions("febs:icons:view")
    public String getFebsIcon() {
        return FebsUtil.view("system/menu/icon");
    }
    @GetMapping( "/others/febs/icon")
    @RequiresPermissions("febs:icons:view")
    public String febsIcon() {
        return FebsUtil.view("system/menu/icon");
    }


}
