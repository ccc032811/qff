package com.neefull.fsp.web.system.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neefull.fsp.common.util.EncryptUtil;
import com.neefull.fsp.web.common.authentication.ShiroRealm;
import com.neefull.fsp.web.common.entity.FebsConstant;
import com.neefull.fsp.web.common.entity.QueryRequest;
import com.neefull.fsp.web.common.utils.FebsUtil;
import com.neefull.fsp.web.common.utils.SortUtil;
import com.neefull.fsp.web.qff.service.IProcessService;
import com.neefull.fsp.web.system.entity.*;
import com.neefull.fsp.web.system.mapper.UserMapper;
import com.neefull.fsp.web.system.service.*;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.Around;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author pei.wang
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
@DS("base")
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private IUserRoleService userRoleService;
    @Autowired
    private ShiroRealm shiroRealm;
    @Autowired
    private IProcessService processService;

    @DS("typt")
    @Override
    public List<User> getAllUser(){
        return this.baseMapper.getAllUser();
    }

    @Override
    public User getUserByName(String username) {
        return this.baseMapper.getUserByName(username);
    }

    @Override
    public void insertUser(User typtUser) {
        this.baseMapper.saveReturnPrimaryKey(typtUser);
    }

    @Override
    public User findUserById(String userId) {
        return  this.baseMapper.selectById(userId);
    }

    @Override
    public User findByName(String username) {
        return this.baseMapper.findByName(username);
    }

    @Override
    public IPage<User> findUserDetail(User user, QueryRequest request) {
        Page<User> page = new Page<>(request.getPageNum(), request.getPageSize());
        SortUtil.handlePageSort(request, page, "userId", FebsConstant.ORDER_ASC, false);
        return this.baseMapper.findUserDetailPage(page, user);
    }

    /**
     * 获取所有的使用用户(不包括系统管理员)
     * @return
     */
    @Override
    public List<User> getAllUseUserLst() {
        return this.baseMapper.getAllUseUserLst();
    }

    @Override
    public IPage<User> findSysUserDetail(User user, QueryRequest request) {
        Page<User> page = new Page<>(request.getPageNum(), request.getPageSize());
        SortUtil.handlePageSort(request, page, "userId", FebsConstant.ORDER_ASC, false);
        return this.baseMapper.findSysUserDetailPage(page, user);
    }

    @Override
    public User findUserDetail(String username) {
        User param = new User();
        param.setUsername(username);
        List<User> users = this.baseMapper.findUserDetail(param);
        return CollectionUtils.isNotEmpty(users) ? users.get(0) : null;
    }

    @Override
    public User findSysUserDetail(String username) {
        User param = new User();
        param.setUsername(username);
        List<User> users = this.baseMapper.findSysUserDetail(param);
        return CollectionUtils.isNotEmpty(users) ? users.get(0) : null;
    }

    @Override
    @Transactional
    public void updateLoginTime(String username) {
        User user = new User();
        user.setLastLoginTime(new Date());
        this.baseMapper.update(user, new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    @Transactional
    public void createUser(User user) {
        user.setCreateTime(new Date());
        user.setStatus(User.STATUS_VALID);
        user.setAvatar(User.DEFAULT_AVATAR);
        user.setTheme(User.THEME_BLACK);
        user.setIsTab(User.TAB_OPEN);
        user.setPassword(EncryptUtil.encrypt(User.DEFAULT_PASSWORD,FebsConstant.AES_KEY));
        user.setUserType(User.USERTYPE_SYSTEM);
        save(user);
        // 保存用户角色
        String[] roles = user.getRoleId().split(StringPool.COMMA);
        setUserRoles(user, roles);
    }

    @Override
    @Transactional
    public void deleteUsers(String[] userIds) {
        List<String> list = Arrays.asList(userIds);
        processService.deleteProcessCommit(userIds);
        // 删除用户
        this.removeByIds(list);
        // 删除关联角色
        this.userRoleService.deleteUserRolesByUserId(list);


    }

    @Override
    @Transactional
    public void updateUser(User user) {

        processService.addProcessCommit(user);
        // 更新用户
        user.setPassword(null);
        user.setUsername(null);
        user.setModifyTime(new Date());
        updateById(user);
        // 更新关联角色
        this.userRoleService.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getUserId()));
        String[] roles = user.getRoleId().split(StringPool.COMMA);
        setUserRoles(user, roles);

        User currentUser = FebsUtil.getCurrentUser();
        if (StringUtils.equalsIgnoreCase(currentUser.getUsername(), user.getUsername())) {
            shiroRealm.clearCache();
        }


    }

    @Override
    @Transactional
    public void resetPassword(String[] usernames) {
        Arrays.stream(usernames).forEach(username -> {
            User user = new User();
            user.setPassword(EncryptUtil.encrypt(User.DEFAULT_PASSWORD,FebsConstant.AES_KEY));
            this.baseMapper.update(user, new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        });
    }

    @Override
    @Transactional
    public void regist(String username, String password) {
        User user = new User();
        user.setPassword(EncryptUtil.encrypt(password,FebsConstant.AES_KEY));
        user.setUsername(username);
        user.setCreateTime(new Date());
        user.setStatus(User.STATUS_VALID);
        user.setSex(User.SEX_UNKNOW);
        user.setAvatar(User.DEFAULT_AVATAR);
        user.setTheme(User.THEME_BLACK);
        user.setIsTab(User.TAB_OPEN);
        user.setDescription("注册用户");
        this.save(user);

        UserRole ur = new UserRole();
        ur.setUserId(user.getUserId());
        ur.setRoleId(2L); // 注册用户角色 ID
        this.userRoleService.save(ur);
    }



    @Override
    @Transactional
    public void updateAvatar(String username, String avatar) {
        User user = new User();
        user.setAvatar(avatar);
        user.setModifyTime(new Date());
        this.baseMapper.update(user, new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    @Transactional
    public void updateTheme(String username, String theme, String isTab) {
        User user = new User();
        user.setTheme(theme);
        user.setIsTab(isTab);
        user.setModifyTime(new Date());
        this.baseMapper.update(user, new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    @Transactional
    public void updateProfile(User user) {
        user.setUsername(null);
        user.setRoleId(null);
        user.setPassword(null);
        updateById(user);
    }

    @Override
    @Transactional
    public void examineUsers(String[] ids) {
        LambdaUpdateWrapper<User> lambdaQueryWrapper = new LambdaUpdateWrapper<>();
        User currentUser = FebsUtil.getCurrentUser();
        User user = new User();

         for(String id:ids)
         {
             long lid = Long.valueOf(id);
             this.baseMapper.updateUserAuthStatus(lid, User.AUTH_STATUS_SUCCESS);
             //再去t_auth_freelancer表和t_auth_corp表更新实名状态
             user = this.baseMapper.selectById(lid);

         }
    }

    /**
     * 首页统计图-用户分布情况
     * @return 用户分布数据
     */
    @Override
    public List<Map<String, String>> getUserDistribution() {
        return this.baseMapper.getUserDistribution();
    }



    @Override
    public List<User> findUserByDepartName(String name) {
        List<User> user = this.baseMapper.findUserByDepartName(name);
        return user;
    }

    @Override
    public List<User> findUserByRoleId(Integer id) {
        List<User> user = this.baseMapper.findUserByRoleId(id);
        return user;
    }

    private void setUserRoles(User user, String[] roles) {
        List<UserRole> userRoles = new ArrayList<>();
        Arrays.stream(roles).forEach(roleId -> {
            UserRole ur = new UserRole();
            ur.setUserId(user.getUserId());
            ur.setRoleId(Long.valueOf(roleId));
            userRoles.add(ur);
        });
        userRoleService.saveBatch(userRoles);
    }

    /**
     * 创建用户信息并返回带主键的实体
     * @return
     */
    private User createUserInfo(String userName, String email, String mobile, String userType){
        User user = new User();
        user.setUsername(userName);
        user.setPassword(EncryptUtil.encrypt(User.DEFAULT_PASSWORD,FebsConstant.AES_KEY));
        user.setDeptId(0L);
        user.setEmail(email);
        user.setMobile(mobile);
        user.setStatus(User.STATUS_VALID);
        user.setSex(User.SEX_UNKNOW);
        user.setIsTab(User.TAB_OPEN);
        user.setTheme(User.THEME_BLACK);
        user.setAvatar(User.DEFAULT_AVATAR);
        user.setUserType(userType);
        user.setAuthStatus(User.AUTH_STATUS_DEFAULT);
        user.setCardStatus(User.CARD_STATUS_DEFAULT);
        //新增并返回id
        this.baseMapper.saveReturnPrimaryKey(user);
        return user;
    }
}
