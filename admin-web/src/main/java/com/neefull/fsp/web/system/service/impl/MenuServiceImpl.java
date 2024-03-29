package com.neefull.fsp.web.system.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neefull.fsp.web.common.authentication.ShiroRealm;
import com.neefull.fsp.web.common.entity.MenuTree;
import com.neefull.fsp.web.common.utils.TreeUtil;
import com.neefull.fsp.web.qff.service.IProcessService;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.system.entity.Menu;
import com.neefull.fsp.web.system.entity.User;
import com.neefull.fsp.web.system.mapper.MenuMapper;
import com.neefull.fsp.web.system.mapper.RoleMenuMapper;
import com.neefull.fsp.web.system.service.IMenuService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.jni.Proc;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author pei.wang
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
@DS("base")
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements IMenuService {

    @Autowired
    private IProcessService processService;
    @Autowired
    private RoleMenuMapper roleMenuMapper;
    @Autowired
    private ShiroRealm shiroRealm;

    @Override
    public List<Menu> findUserPermissions(String username) {
        return this.baseMapper.findUserPermissions(username);
    }

    @Override
    public MenuTree<Menu> findUserMenus(User user) {
        List<Menu> menus = this.baseMapper.findUserMenus(user.getUsername());
        List<MenuTree<Menu>> trees = this.convertMenus(menus,user);
        return TreeUtil.buildMenuTree(trees);
    }

    @Override
    public MenuTree<Menu> findMenus(Menu menu) {
        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(menu.getMenuName())) {
            queryWrapper.lambda().like(Menu::getMenuName, menu.getMenuName());
        }
        queryWrapper.lambda().orderByAsc(Menu::getOrderNum);
        List<Menu> menus = this.baseMapper.selectList(queryWrapper);
        List<MenuTree<Menu>> trees = this.convertMenus(menus);

        return TreeUtil.buildMenuTree(trees);
    }

    @Override
    public List<Menu> findMenuList(Menu menu) {
        QueryWrapper<Menu> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(menu.getMenuName())) {
            queryWrapper.lambda().like(Menu::getMenuName, menu.getMenuName());
        }
        queryWrapper.lambda().orderByAsc(Menu::getMenuId).orderByAsc(Menu::getOrderNum);
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createMenu(Menu menu) {
        menu.setCreateTime(new Date());
        this.setMenu(menu);
        this.baseMapper.insert(menu);
    }


    @Override
    @Transactional
    public void updateMenu(Menu menu) {
        menu.setModifyTime(new Date());
        this.setMenu(menu);
        this.baseMapper.updateById(menu);

        shiroRealm.clearCache();
    }

    @Override
    @Transactional
    public void deleteMeuns(String menuIds) {
        String[] menuIdsArray = menuIds.split(StringPool.COMMA);
        for (String menuId : menuIdsArray) {
            // 递归删除这些菜单/按钮
            this.baseMapper.deleteMenus(menuId);
            this.roleMenuMapper.deleteRoleMenus(menuId);
        }

        shiroRealm.clearCache();
    }

    @Override
    @DS("typt")
    public List<Menu> findUserPermissionList(String userName) {
        return this.baseMapper.findUserPermissionList(userName);
    }


    private List<MenuTree<Menu>> convertMenus(List<Menu> menus) {
        List<MenuTree<Menu>> trees = new ArrayList<>();
        for (Menu menu : menus) {
            MenuTree<Menu> tree = new MenuTree<>();
            tree.setId(String.valueOf(menu.getMenuId()));
            tree.setParentId(String.valueOf(menu.getParentId()));
            tree.setTitle(menu.getMenuName());
            tree.setIcon(menu.getIcon());
            tree.setHref(menu.getUrl());
            tree.setData(menu);
            trees.add(tree);
        }
        return trees;
    }


    private List<MenuTree<Menu>> convertMenus(List<Menu> menus,User user) {
        List<String> list = processService.findPrcessName(user);
        List<MenuTree<Menu>> trees = new ArrayList<>();
        for (Menu menu : menus) {
            MenuTree<Menu> tree = new MenuTree<>();
            tree.setId(String.valueOf(menu.getMenuId()));
            tree.setParentId(String.valueOf(menu.getParentId()));
            tree.setTitle(menu.getMenuName());
            tree.setIcon(menu.getIcon());
            tree.setHref(menu.getUrl());
            tree.setData(menu);
            if(CollectionUtils.isNotEmpty(list)){
               if(list.contains(menu.getMenuName())){
                   tree.setExist("1");
               }
            }
            trees.add(tree);
        }
//        menus.forEach(menu -> {
//            MenuTree<Menu> tree = new MenuTree<>();
//            tree.setId(String.valueOf(menu.getMenuId()));
//            tree.setParentId(String.valueOf(menu.getParentId()));
//            tree.setTitle(menu.getMenuName());
//            tree.setIcon(menu.getIcon());
//            tree.setHref(menu.getUrl());
//            tree.setData(menu);
//            trees.add(tree);
//        });
        return trees;
    }

    private void setMenu(Menu menu) {
        if (menu.getParentId() == null)
            menu.setParentId(Menu.TOP_NODE);
        if (Menu.TYPE_BUTTON.equals(menu.getType())) {
            menu.setUrl(null);
            menu.setIcon(null);
        }
    }
}
