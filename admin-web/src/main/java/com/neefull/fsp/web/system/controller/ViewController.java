package com.neefull.fsp.web.system.controller;

import com.neefull.fsp.web.common.authentication.ShiroHelper;
import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.common.entity.FebsConstant;
import com.neefull.fsp.web.common.utils.DateUtil;
import com.neefull.fsp.web.common.utils.FebsUtil;
import com.neefull.fsp.web.system.entity.*;
import com.neefull.fsp.web.system.service.IAuthCorpService;
import com.neefull.fsp.web.system.service.IAuthFreelancerService;
import com.neefull.fsp.web.system.service.IProjectService;
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
    @Autowired
    private IAuthCorpService authCorpService;
    @Autowired
    private IAuthFreelancerService authFreelancerService;
    @Autowired
    private IProjectService projectService;

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
        model.addAttribute("user", userService.findByName(user.getUsername())); // 获取实时的用户信息
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

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/user")
    @RequiresPermissions("user:view")
    public String systemUser() {
        return FebsUtil.view("system/user/user");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/user/add")
    @RequiresPermissions("user:add")
    public String systemUserAdd() {
        return FebsUtil.view("system/user/userAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/user/detail/{username}")
    @RequiresPermissions("user:view")
    public String systemUserDetail(@PathVariable String username, Model model) {
        resolveUserModel(username, model, true);
        return FebsUtil.view("system/user/userDetail");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/user/update/{username}")
    @RequiresPermissions("user:update")
    public String systemUserUpdate(@PathVariable String username, Model model) {
        resolveUserModel(username, model, false);
        return FebsUtil.view("system/user/userUpdate");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/sendMessage")
    @RequiresPermissions("message:send")
    public String sendMessage() {
        return FebsUtil.view("system/message/sendMessage");
    }

    //**************************************项目管理模块 start *********************************************
    @GetMapping(FebsConstant.VIEW_PREFIX + "system/project")
    @RequiresPermissions("project:view")
    public String systemProject() {
        return FebsUtil.view("system/project/project");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/project/detail/{id}")
    @RequiresPermissions("project:view")
    public String systemProjectDetail(@PathVariable String id, Model model) {
        projectDetailModel(id, model, true);
        return FebsUtil.view("system/project/projectDetail");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/project/settle/{id}")
    @RequiresPermissions("project:settle")
    public String systemProjectSettle(@PathVariable String id, Model model) {
        model.addAttribute("projectId", id);
        return FebsUtil.view("system/project/projectSettle");
    }
    //**************************************项目管理模块 end *********************************************

    //**************************************用户管理模块 start *********************************************
    @GetMapping(FebsConstant.VIEW_PREFIX + "system/sysUser")
    @RequiresPermissions("sysuser:view")
    public String sysUser() {
        return FebsUtil.view("system/user/sysuser");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/sysUser/add")
    @RequiresPermissions("sysuser:add")
    public String sysUserAdd() {
        return FebsUtil.view("system/user/sysuserAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/sysUser/detail/{username}")
    @RequiresPermissions("sysuser:view")
    public String sysUserDetail(@PathVariable String username, Model model) {
        resolveSysUserModel(username, model, true);
        return FebsUtil.view("system/user/sysuserDetail");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/sysUser/update/{username}")
    @RequiresPermissions("sysuser:update")
    public String sysUserUpdate(@PathVariable String username, Model model) {
        resolveSysUserModel(username, model, false);
        return FebsUtil.view("system/user/sysuserUpdate");
    }
    //**************************************用户管理模块 end *********************************************

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/role")
    @RequiresPermissions("role:view")
    public String systemRole() {
        return FebsUtil.view("system/role/role");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/menu")
    @RequiresPermissions("menu:view")
    public String systemMenu() {
        return FebsUtil.view("system/menu/menu");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "system/dept")
    @RequiresPermissions("dept:view")
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

    private void resolveUserModel(String username, Model model, Boolean transform) {
        User user = userService.findByName(username);
        model.addAttribute("user", user);
        if(User.USERTYPE_FREELANCER.equals(user.getUserType())){   // 0：自由职业者
            AuthFreelancer authFreelancer = authFreelancerService.findByUserId(user.getUserId());
            model.addAttribute("authLancer", authFreelancer != null ? authFreelancer : new AuthFreelancer());
            model.addAttribute("authCorp", new AuthCorp());
        }else if(User.USERTYPE_CORP.equals(user.getUserType())){   // 1:企业用户
            AuthCorp authCorp = authCorpService.findByUserId(user.getUserId());
            model.addAttribute("authLancer", new AuthFreelancer());
            model.addAttribute("authCorp", authCorp != null ? authCorp : new AuthCorp());
        }else if(User.USERTYPE_SYSTEM.equals(user.getUserType())){   //2:系统用户
            model.addAttribute("authLancer", new AuthFreelancer());
            model.addAttribute("authCorp", new AuthCorp());
        }
        if (transform) {
            String ssex = user.getSex();
            if (User.SEX_MALE.equals(ssex)) user.setSex("男");
            else if (User.SEX_FEMALE.equals(ssex)) user.setSex("女");
            else user.setSex("保密");
        }
        if (user.getLastLoginTime() != null)
            model.addAttribute("lastLoginTime", DateUtil.getDateFormat(user.getLastLoginTime(), DateUtil.FULL_TIME_SPLIT_PATTERN));
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

    /**
     * 项目信息详情页
     * @param id 项目id
     * @param model model
     * @param transform transform
     */
    private void projectDetailModel(String id, Model model, Boolean transform){
        Project project = projectService.getProjectById(id);
        model.addAttribute("project", project);
    }
}
