package com.neefull.fsp.web.qff.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;

import java.io.Serializable;

/**
 * @Author: chengchengchu
 * @Date: 2020/9/17  16:39
 */
@Excel("国际到货超温，无温度计")
public class Temperature implements Serializable {

    private static final long serialVersionUID = -7910797280094285687L;
    /**
     *  QFF编号
     */
    @TableField("number")
    @ExcelField(value = "QFF编号")
    private String number;
    /**
     *  事件描述
     */
    @TableField("remark")
    @ExcelField(value = "事件描述")
    private String remark;
    /**
     *  开始时间
     */
    @TableField("start_date")
    @ExcelField(value = "开始时间")
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
    @ExcelField(value = "产品信息")
    private String message;
    /**
     *  数量
     */
    @TableField("count")
    @ExcelField(value = "总数量")
    private String count;
    /**
     *  罗氏QA处理意见
     */
    @TableField("r_conf")
    @ExcelField(value = "罗氏QA处理意见")
    private String rConf;
    /**
     *  回复日期
     */
    @TableField("rep_date")
    @ExcelField(value = "回复日期")
    private String repDate;
    /**
     *  变更记录
     */
    @TableField("alteration")
    @ExcelField(value = "变更记录")
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
