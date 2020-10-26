package com.neefull.fsp.web.qff.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 近效期QFF
 *
 * @Author: chengchengchu
 * @Date: 2019/11/26  20:25
 */

@TableName(value = "qff_recent")
public class Recent implements Serializable {
    private static final long serialVersionUID = -6883719899970874252L;

    /**
     *  主键id
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
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
     *  类型
     */
    @TableField("stage")
    private String stage;
    /**
     *  变更记录
     */
    @TableField("alteration")
    private String alteration;
    /**
     *  备注
     */
    @TableField("comment")
    private String comment;

    /**
     *  图片
     */
    @TableField(exist = false)
    private String images;
    /**
     *  状态码  1新建  2 审核中 3 完结  4异常
     */
    @TableField("status")
    private Integer status;
    /**
     *  创建日期
     */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createTime;
    /**
     *  更新日期
     */
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date updateTime;

    /**
     * 开始时间
     */
    @TableField(exist = false)
    private String startTime;
    /**
     * 结束时间
     */
    @TableField(exist = false)
    private String endTime;
    /**
     * 当前页面数据量
     */
    @TableField(exist = false)
    private Integer pageSize;
    /**
     * 当前页码
     */
    @TableField(exist = false)
    private Integer pageNum;
    /**
     * 是否能审核
     */
    @TableField(exist = false)
    private Integer isAllow;

    @TableField(exist = false)
    private Integer att;
    

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getAlteration() {
        return alteration;
    }

    public void setAlteration(String alteration) {
        this.alteration = alteration;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getIsAllow() {
        return isAllow;
    }

    public void setIsAllow(Integer isAllow) {
        this.isAllow = isAllow;
    }

    public Integer getAtt() {
        return att;
    }

    public void setAtt(Integer att) {
        this.att = att;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
