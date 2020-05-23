package com.neefull.fsp.web.job.task;

import com.neefull.fsp.web.qff.config.SoapUrlProperties;
import com.neefull.fsp.web.qff.service.IAttachmentService;
import com.neefull.fsp.web.qff.service.ICommodityService;
import com.neefull.fsp.web.qff.service.IProcessService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: chengchengchu
 * @Date: 2020/5/22  14:51
 */

@Component
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class AcquireOneSoap {

    @Autowired
    private StartSoap startSoap;

    @Transactional
    public void startOneSap(String params){
        String[] split = params.split(",");
        String seacheDate = split[0];
        String fromTime = split[1];
        String toTime = split[2];
        String number = "";
        if(split.length ==4){
            number = split[3];
        }
        startSoap.getMessage(seacheDate,fromTime,toTime,number);

    }
}
