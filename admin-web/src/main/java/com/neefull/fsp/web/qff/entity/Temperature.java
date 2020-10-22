package com.neefull.fsp.web.qff.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;

import java.io.Serializable;

/**
 * @Author: chengchengchu
 * @Date: 2020/9/17  16:39
 */
public class Temperature implements Serializable {

    private static final long serialVersionUID = -7910797280094285687L;
    /**
     *  QFF编号
     */
    @TableField("number")
    private String number;
    /**
     *  事件描述
     */
    @TableField("remark")
    private String remark;
    /**
     *  开始时间
     */
    @TableField("start_date")
    private String startDate;
    /**
     *  工厂
     */
    @TableField("factory")
    private String factory;
    /**
     *  产品信息
     */
    @TableField("message")
    private String message;
    /**
     *  数量
     */
    @TableField("count")
    private String count;
    /**
     *  罗氏QA处理意见
     */
    @TableField("r_conf")
    private String rConf;
    /**
     *  回复日期
     */
    @TableField("rep_date")
    private String repDate;
    /**
     *  变更记录
     */
    @TableField("alteration")
    private String alteration;


    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getrConf() {
        return rConf;
    }

    public void setrConf(String rConf) {
        this.rConf = rConf;
    }

    public String getRepDate() {
        return repDate;
    }

    public void setRepDate(String repDate) {
        this.repDate = repDate;
    }

    public String getAlteration() {
        return alteration;
    }

    public void setAlteration(String alteration) {
        this.alteration = alteration;
    }
}
