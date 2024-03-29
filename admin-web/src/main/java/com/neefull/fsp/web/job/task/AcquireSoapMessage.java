package com.neefull.fsp.web.job.task;

import com.neefull.fsp.web.common.controller.BaseController;
import com.neefull.fsp.web.qff.service.ICommodityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**自动QFF数据
 * @Author: chengchengchu
 * @Date: 2020/3/11  15:34
 */
@Slf4j
@Component
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class AcquireSoapMessage extends BaseController {

    private static final String ZORE_TIME = "00:00:00";
    private static final String END_TIME = "23:59:59";
    private static final String BEFORE_TIME = "00:08:00";

    @Autowired
    private ICommodityService commodityService;
    @Autowired
    private StartSoap startSoap;

    @Transactional
    public void startSapMessage() {
        //获取更新时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        //系统当前时间
        String seacheDate =DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss");
        String lastDate = null;
        String fromTime = "";
        try {
            //获取表里面的最新时间
            lastDate = commodityService.selectLastTime();
        } catch (Exception e) {
            log.info("当前数据库无数据，初始化起始时间");
            fromTime = ZORE_TIME;
        }
        //截取最新时间的
        if(StringUtils.isNotEmpty(lastDate)){
            if (StringUtils.isNotEmpty(lastDate)&&lastDate.startsWith(seacheDate.split(" ")[0])) {
                fromTime = lastDate.split(" ")[1];
            } else {
                fromTime = ZORE_TIME;
            }
        }

        String toTime = DateFormatUtils.format(new Date(), "HH:mm:ss");
        boolean before = false;
        try {
            before = simpleDateFormat.parse(seacheDate.split(" ")[1]).getTime() < simpleDateFormat.parse(BEFORE_TIME).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(fromTime.equals(ZORE_TIME)&&before){
            //如果天数更改了  那么从00：00：00开始
            Calendar instance = Calendar.getInstance();
            instance.add(Calendar.DAY_OF_MONTH,-1);
            String date = DateFormatUtils.format(instance.getTime(), "yyyy-MM-dd");
            String startTime = ZORE_TIME;
            if(StringUtils.isNotEmpty(lastDate)&&lastDate.startsWith(date)){
                startTime = lastDate.split(" ")[1];
            }
            startSoap.getMessage(date,startTime,END_TIME,"",null);

        }else {
            //没有就从最新时间开始
            startSoap.getMessage(seacheDate.split(" ")[0],fromTime,toTime,"",null);
        }
    }

}
