package com.neefull.fsp.app.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neefull.fsp.app.entity.Project;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author pei.wang
 */
public interface ProjectMapper extends BaseMapper<Project> {

    /**
     * 用户首页项目查询
     *
     * @param page 分页对象
     * @param project 项目对象，用于传递查询条件
     * @return Ipage
     */
    IPage<Project> personalHome(Page page, @Param("project") Project project);

    /**
     * 企业首页项目查询
     *
     * @param page 分页对象
     * @param project 项目对象，用于传递查询条件
     * @return Ipage
     */
    IPage<Project> corpHome(Page<Project> page, @Param("project") Project project);

    @Update("update t_project set sign_num=#{project.signNum} where id=#{project.id}")
    int  updateProjectSignNum(@Param("project") Project project);
}