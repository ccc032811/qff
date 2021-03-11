package com.neefull.fsp.web.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neefull.fsp.web.common.entity.QueryRequest;
import com.neefull.fsp.web.system.entity.User;

import java.net.UnknownServiceException;
import java.util.List;
import java.util.Map;

/**
 * @author pei.wang
 */
public interface IUserService extends IService<User> {

    /**
     * 通过用户名查找用户
     *
     * @param username 用户名
     * @return 用户
     */
    User findByName(String username);

    /**
     * 查找用户详细信息
     *
     * @param request request
     * @param user    用户对象，用于传递查询条件
     * @return IPage
     */
    IPage<User> findUserDetail(User user, QueryRequest request);

    /**
     * 获取所有的使用用户(不包括系统管理员)
     * @return
     */
    List<User> getAllUseUserLst();

    /**
     * 查找用户详细信息--系统用户
     *
     * @param request request
     * @param user    用户对象，用于传递查询条件
     * @return IPage
     */
    IPage<User> findSysUserDetail(User user, QueryRequest request);

    /**
     * 通过用户名查找用户详细信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    User findUserDetail(String username);

    /**
     * 通过用户名查找用户详细信息--系统用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User findSysUserDetail(String username);

    /**
     * 更新用户登录时间
     *
     * @param username 用户名
     */
    void updateLoginTime(String username);

    /**
     * 新增用户
     *
     * @param user user
     */
    void createUser(User user);

    /**
     * 删除用户
     *
     * @param userIds 用户 id数组
     */
    void deleteUsers(String[] userIds);

    /**
     * 修改用户
     *
     * @param user user
     */
    void updateUser(User user);

    /**
     * 重置密码
     *
     * @param usernames 用户名数组
     */
    void resetPassword(String[] usernames);

    /**
     * 注册用户
     *
     * @param username 用户名
     * @param password 密码
     */
    void regist(String username, String password);


    /**
     * 更新用户头像
     *
     * @param username 用户名
     * @param avatar   用户头像
     */
    void updateAvatar(String username, String avatar);

    /**
     * 修改用户系统配置（个性化配置）
     *
     * @param username 用户名称
     * @param theme    主题风格
     * @param isTab    是否开启 TAB
     */
    void updateTheme(String username, String theme, String isTab);

    /**
     * 更新个人信息
     *
     * @param user 个人信息
     */
    void updateProfile(User user);

    void examineUsers(String[] ids);

    /**
     * 首页统计图-用户分布情况
     * @return 用户分布数据
     */
    List<Map<String, String>> getUserDistribution();



    /**查出部门下所有的用户
     * @param name
     * @return
     */
    List<User> findUserByDepartName(String name);

    /**根据角色id查询所有的用户
     * @param id
     * @return
     */
    List<User> findUserByRoleId(Integer id);


    List<User> getAllUser();

    /**根据名字 查询用户
     * @param username
     * @return
     */
    User getUserByName(String username);

    /**插入用户
     * @param typtUser
     */
    void insertUser(User typtUser);

    User findUserById(String userId);
}
