package com.neefull.fsp.app.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neefull.fsp.app.annotation.AuthToken;
import com.neefull.fsp.app.config.FspState;
import com.neefull.fsp.app.entity.*;
import com.neefull.fsp.app.mapper.ProjectEnrMapper;
import com.neefull.fsp.app.mapper.ProjectMapper;
import com.neefull.fsp.app.mapper.ProjectTeamMapper;
import com.neefull.fsp.app.service.IProjectEnrService;
import com.neefull.fsp.app.service.IProjectService;
import com.neefull.fsp.app.service.IProjectTeamService;
import com.neefull.fsp.common.entity.FebsResponse;
import com.neefull.fsp.common.entity.QueryRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 报名处理
 *
 * @author pei.wang
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/enroll")
public class EnrollmentController {
    @Autowired
    IProjectService projectService;
    @Autowired
    IProjectEnrService projectEnrService;
    @Autowired
    ProjectEnrMapper projectEnrMapper;
    @Autowired
    ProjectMapper projectMapper;
    @Autowired
    IProjectTeamService projectTeamService;

    /**
     * 自由职业者报名项目
     *
     * @return
     * @
     */

    @RequestMapping(value = "/enrollmentProject", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    @AuthToken
    public String enrollmentProject(@RequestBody ProjectEnrollment projectEnrollment, HttpServletRequest httpServletRequest) {
        long userId = (long) httpServletRequest.getAttribute("userId");
        //TODO
        //  long userId = 9;
        //设置报名用户
        projectEnrollment.setUserId(userId);
        int result = projectEnrService.saveProjectEnrollment(projectEnrollment);
        if (result > 0) {
            return new FebsResponse().success().data("").message("报名成功,等待企业审核").toJson();
        } else if (result < 0) {
            return new FebsResponse().fail().data("").message("未通过实名认证，暂无法报名项目").toJson();
        } else {
            return new FebsResponse().error().data("").message("服务故障").toJson();
        }
    }

    /**
     * 自由职业者查询报名项目信息
     *
     * @param projectEnrollPage
     * @param httpServletRequest
     * @return
     */

    @RequestMapping(value = "/queryFreelencerEnrollment", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    @AuthToken
    public String queryFreelencerEnrollment(@RequestBody ProjectEnrollPage projectEnrollPage, HttpServletRequest httpServletRequest) {
        long userId = (long) httpServletRequest.getAttribute("userId");
        //long userId = 9;
        QueryProjectEncroll queryProjectEncroll = projectEnrollPage.getQueryProjectEncroll();
        QueryRequest queryRequest = projectEnrollPage.getQueryRequest();
        if (null == queryProjectEncroll || null == queryRequest) {
            return new FebsResponse().fail().data(null).message("参数不合法").toJson();
        }
        //设置报名用户
        queryProjectEncroll.setUserId(userId);
        Map<String, Object> dataTable = queryRequest.getDataTable(this.projectEnrService.queryFreelencerEnrollment(queryProjectEncroll, queryRequest));
        if (0 == (long) dataTable.get("total")) {
            return new FebsResponse().success().data(dataTable).message("未查询到相关数据").toJson();
        } else {
            return new FebsResponse().success().data(dataTable).message("数据查询成功").toJson();
        }
    }

    /**
     * 企业查询相关项目报名用户
     *
     * @param projectEnrollPage
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/querySignUser", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    @AuthToken
    public String querySignUser(@RequestBody ProjectEnrollPage projectEnrollPage, HttpServletRequest httpServletRequest) {
        long userId = (long) httpServletRequest.getAttribute("userId");
        // long userId = 34;
        QueryProjectEncroll queryProjectEncroll = projectEnrollPage.getQueryProjectEncroll();
        QueryRequest queryRequest = projectEnrollPage.getQueryRequest();
        if (null == queryProjectEncroll || null == queryRequest) {
            return new FebsResponse().fail().data(null).message("参数不合法").toJson();
        }
        //设置报名用户
        queryProjectEncroll.setProjectOwner(userId);
        Map<String, Object> dataTable = queryRequest.getDataTable(this.projectEnrService.queryFreelencerEnrollment(queryProjectEncroll, queryRequest));
        if (0 == (long) dataTable.get("total")) {
            return new FebsResponse().success().data(dataTable).message("未查询到相关数据").toJson();
        } else {
            return new FebsResponse().success().data(dataTable).message("数据查询成功").toJson();
        }
    }

    /**
     * 企业确认报名用户
     *
     * @param projectEnrollment
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/confirmSignUser", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    @AuthToken
    public String confirmSignUser(@RequestBody ProjectEnrollment projectEnrollment, HttpServletRequest httpServletRequest) {
        int result = projectEnrMapper.confirmSignUser(projectEnrollment);
        if ((result > 0)) {
            createProjectTeam(projectEnrollment.getProjectId(), projectEnrollment.getUserId());
            return new FebsResponse().success().data("").message("确认用户报名成功").toJson();
        } else {
            return new FebsResponse().fail().data("").message("确认用户报名失败").toJson();
        }
    }

    /**
     * 用户取消报名
     *
     * @param projectEnrollment
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "/cancelSignup", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    @AuthToken
    public String cancelSignup(@RequestBody ProjectEnrollment projectEnrollment, HttpServletRequest httpServletRequest) {
        long userId = (long) httpServletRequest.getAttribute("userId");
        projectEnrollment.setUserId(userId);
        if (projectEnrService.cancelSignup(projectEnrollment) > 0) {
            return new FebsResponse().success().data("").message("取消报名成功").toJson();
        } else {
            return new FebsResponse().fail().data("").message("取消报名失败").toJson();
        }
    }

    /**
     * 生成项目团队的规则，确认过用户，则在项目团队表中插入一个用户信息
     * 此时状态是0 代表团队尚未生效，如果某个项目的报名人数>=2的时候，则
     * 改变团队的状态，使之为1，查询项目团队，只查询为1 的即可
     */
    @Autowired
    ProjectTeamMapper projectTeamMapper;

    @Async
    public boolean createProjectTeam(long projectId, long userId) {
        Project project = new Project();
        //查询项目名称
        String projectName = projectMapper.getProjectName(projectId);
        //生成项目团队信息
        ProjectTeam projectTeam = new ProjectTeam();
        projectTeam.setUserId(userId);
        projectTeam.setProjectId(projectId);
        projectTeam.setTeamName("[" + projectName + "]" + "工作团队");
        boolean saveResult = projectTeamService.saveProjectTeam(projectTeam);
        if (saveResult) {
            //查询当前团队有效人数，如果>=2,则使团队生效
            LambdaQueryWrapper<ProjectTeam> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.groupBy(ProjectTeam::getProjectId);
            lambdaQueryWrapper.eq(ProjectTeam::getProjectId, projectId);
            lambdaQueryWrapper.ne(ProjectTeam::getTeamState, -1);
            int count = projectTeamMapper.selectCount(lambdaQueryWrapper);
            if (count >= 2) {
                projectTeamMapper.changeProjectTeamState(projectId, FspState.PROJECT_TEAM_STATE.Effective.getState());
            }
        }
        return saveResult;
    }
}
