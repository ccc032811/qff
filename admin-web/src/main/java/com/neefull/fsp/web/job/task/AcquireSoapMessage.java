package com.neefull.fsp.web.job.task;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.qff.config.SendMailProperties;
import com.neefull.fsp.web.qff.config.SoapUrlProperties;
import com.neefull.fsp.web.qff.entity.Attachment;
import com.neefull.fsp.web.qff.entity.Commodity;
import com.neefull.fsp.web.qff.service.IAttachmentService;
import com.neefull.fsp.web.qff.service.ICommodityService;
import com.neefull.fsp.web.qff.service.IProcessService;
import com.neefull.fsp.web.qff.utils.ProcessConstant;
import com.neefull.fsp.web.qff.utils.SapWsUtils;
import com.neefull.fsp.web.qff.utils.XmlUtils;
import com.neefull.fsp.web.system.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Author: chengchengchu
 * @Date: 2020/3/11  15:34
 */
@Slf4j
@Component
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class AcquireSoapMessage extends BaseController {

    @Autowired
    private ICommodityService commodityService;
    @Autowired
    private StartSoap startSoap;

    @Transactional
    public void startSapMessage() {
        //获取更新时间
        String seacheDate = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
        String lastDate = commodityService.selectLastTime();
        String fromTime = "";
        if (StringUtils.isNotEmpty(lastDate)&&lastDate.startsWith(seacheDate)) {
            fromTime = lastDate.split(" ")[1];
        } else {
            fromTime = "00:00:00";
        }
        String toTime = DateFormatUtils.format(new Date(), "HH:mm:ss");
        startSoap.getMessage(seacheDate,fromTime,toTime,"");
    }

}
