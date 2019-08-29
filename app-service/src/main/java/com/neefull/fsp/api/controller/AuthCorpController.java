package com.neefull.fsp.api.controller;


import com.neefull.common.core.entity.FebsResponse;
import com.neefull.fsp.api.annotation.AuthToken;
import com.neefull.fsp.api.entity.AuthCorp;
import com.neefull.fsp.api.exception.BizException;
import com.neefull.fsp.api.mapper.AuthCorpMapper;
import com.neefull.fsp.api.service.IAuthCorpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 平台账户
 *
 * @author pei.wang
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/auth")
public class AuthCorpController {
    @Autowired
    IAuthCorpService authCorpService;
    @Autowired
    AuthCorpMapper authCorpMapper;

    @RequestMapping(value = "/corpCertification", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    @AuthToken
    public String corpCertification(@RequestBody AuthCorp authCorp, HttpServletRequest httpServletRequest) throws BizException {
        long userId = (long) httpServletRequest.getAttribute("userId");
        authCorp.setUserId(userId);
        //TODO 默认认证通过，保存数据
        int result = authCorpService.saveAuthCorp(authCorp);
        if (result > 0) {
            return new FebsResponse().success().data(result).message("企业申请认证成功,请等待审核").toJson();

        } else {
            return new FebsResponse().fail().data(result).message("申请企业认证失败,请重新录入信息").toJson();

        }

    }

    /**
     * 获取企业提交的认证信息
     *
     * @param authCorp
     * @param httpServletRequest
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/getCorpAuthInfo", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    //@AuthToken
    public String getCorpAuthInfo(@RequestBody AuthCorp authCorp, HttpServletRequest httpServletRequest) throws BizException {
        try {
            long userId = (long) httpServletRequest.getAttribute("userId");
            //long userId = authCorp.getUserId();
            authCorp.setUserId(userId);
            authCorp = authCorpService.queryCorpByUserId(authCorp);
            if (null == authCorp) {
                return new FebsResponse().fail().data("").message("企业尚未提交认证资料").toJson();
            } else {
                return new FebsResponse().success().data(authCorp).message("").toJson();
            }

        } catch (Exception e) {
            throw new BizException(e.getMessage());
        }
    }

    /**
     * 获取企业提交的认证信息
     *
     * @param authCorp
     * @param httpServletRequest
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/updateCorpAuthInfo", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    @AuthToken
    public String updateCorpAuthInfo(@RequestBody AuthCorp authCorp, HttpServletRequest httpServletRequest) throws BizException {
        try {
            long userId = (long) httpServletRequest.getAttribute("userId");
            //long userId = 9;
            authCorp.setUserId(userId);
            if (authCorp.getAuthStatus().equals("1")) {
                authCorp.setAuthpassTime(new Date());
                authCorp.setAuthpassUser(authCorp.getAuthpassUser());
            }
            int result = authCorpService.updateAuthCorp(authCorp);
            if (result <= 0) {
                return new FebsResponse().fail().data("").message("提交修改失败").toJson();
            } else {
                return new FebsResponse().success().data(result).message("修改成功").toJson();
            }

        } catch (RuntimeException e) {
            throw new BizException(e.getMessage());
        }
    }
}
