package com.neefull.fsp.web.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neefull.fsp.web.common.authentication.ShiroRealm;
import com.neefull.fsp.web.common.entity.FebsConstant;
import com.neefull.fsp.web.common.entity.QueryRequest;
import com.neefull.fsp.web.common.utils.SortUtil;
import com.neefull.fsp.web.system.entity.Role;
import com.neefull.fsp.web.system.entity.RoleMenu;
import com.neefull.fsp.web.system.mapper.RoleMapper;
import com.neefull.fsp.web.system.service.IRoleMenuService;
import com.neefull.fsp.web.system.service.IRoleService;
import com.neefull.fsp.web.system.service.IUserRoleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author pei.wang
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    @Autowired
    private IRoleMenuService roleMenuService;
    @Autowired
    private IUserRoleService userRoleService;
    @Autowired
    private ShiroRealm shiroRealm;

    @Override
    public List<Role> findUserRole(String username) {
        return this.baseMapper.findUserRole(username);
    }

    @Override
    public String findUserRoleIds(String username) {
        List<String> userRoleIds = this.baseMapper.findUserRoleIds(username);
        return StringUtils.join(userRoleIds, ",");
    }

    @Override
    public List<Role> findRoles(Role role) {
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(role.getRoleName()))
            queryWrapper.lambda().like(Role::getRoleName, role.getRoleName());
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public IPage<Role> findRoles(Role role, QueryRequest request) {
        Page<Role> page = new Page<>(request.getPageNum(), request.getPageSize());
        SortUtil.handlePageSort(request, page, "createTime", FebsConstant.ORDER_DESC, false);
        return this.baseMapper.findRolePage(page, role);
    }

    @Override
    public Role findByName(String roleName) {
        return this.baseMapper.selectOne(new QueryWrapper<Role>().lambda().eq(Role::getRoleName, roleName));
    }

    @Override
    @Transactional
    public void createRole(Role role) {
        role.setCreateTime(new Date());
        this.baseMapper.insert(role);
        this.saveRoleMenus(role);
    }

    @Override
    @Transactional
    public void updateRole(Role role) {
        role.setModifyTime(new Date());
        this.updateById(role);
        List<String> roleIdList = new ArrayList<>();
        roleIdList.add(String.valueOf(role.getRoleId()));
        this.roleMenuService.deleteRoleMenusByRoleId(roleIdList);
        saveRoleMenus(role);

        shiroRealm.clearCache();
    }

    @Override
    @Transactional
    public void deleteRoles(String roleIds) {
        List<String> list = Arrays.asList(roleIds.split(StringPool.COMMA));
        this.baseMapper.delete(new QueryWrapper<Role>().lambda().in(Role::getRoleId, list));

        this.roleMenuService.deleteRoleMenusByRoleId(list);
        this.userRoleService.deleteUserRolesByRoleId(list);
    }

    private void saveRoleMenus(Role role) {
        if (StringUtils.isNotBlank(role.getMenuIds())) {
            String[] menuIds = role.getMenuIds().split(StringPool.COMMA);
            List<RoleMenu> roleMenus = new ArrayList<>();
            Arrays.stream(menuIds).forEach(menuId -> {
                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setMenuId(Long.valueOf(menuId));
                roleMenu.setRoleId(role.getRoleId());
                roleMenus.add(roleMenu);
            });
            roleMenuService.saveBatch(roleMenus);
        }
    }
}
