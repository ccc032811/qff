package com.neefull.fsp.web.qff.aspect;

import com.alibaba.fastjson.JSON;
import com.neefull.fsp.web.qff.entity.QffLog;
import com.neefull.fsp.web.qff.service.IQffLogService;
import com.neefull.fsp.web.system.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.security.auth.message.AuthException;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @Author: chengchengchu
 * @Date: 2020/1/15  10:45
 */

@Slf4j
@Aspect
@Component
public class QffLogAspect {

    @Autowired
    private IQffLogService qffLogService;

    @Pointcut("@annotation(com.neefull.fsp.web.qff.aspect.Qff)")
    public void pointCut() {
        // do nothing
    }

    @Around(value = "pointCut()")
    public Object aroundAdvice(ProceedingJoinPoint point) throws Throwable {

        //利用aop 将日志记录
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        if (ra == null || sra == null) {
            return point.proceed();
        }

//        HttpServletRequest request = sra.getRequest();
//        log.info("URL : " + request.getRequestURL().toString());
//        log.info("HTTP_METHOD : " + request.getMethod());
//        log.info("IP : " + request.getRemoteAddr());
//        log.info("CLASS_METHOD : " + point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName());
//        log.info("REQUEST ARGS : " + JSON.toJSONString(point.getArgs()));

        long startTime = System.currentTimeMillis();
        Object response = null;
        try {
            response = point.proceed();
            log.info("RESPONSE:{}", response != null ? JSON.toJSONString(response) : "");
            return response;
        } catch (AuthException e) {
            log.info("RESPONSE ERROR:{}", e.getMessage());
            throw e;
        } catch (MethodArgumentNotValidException e) {
            log.info("RESPONSE ERROR:{}", e.getMessage());
            throw e;
        } catch (Throwable e) {
            log.error("RESPONSE ERROR:{}", Arrays.toString(e.getStackTrace()));
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("SPEND TIME : {}ms", (endTime - startTime));

            User user = (User) SecurityUtils.getSubject().getPrincipal();
            QffLog qffLog = new QffLog();
            qffLog.setUsername(user.getUsername());
            qffLog.setResponse(JSON.toJSONString(response));
            qffLog.setTime(endTime - startTime);
            qffLogService.addQffLog(point,qffLog);
        }

    }


}
