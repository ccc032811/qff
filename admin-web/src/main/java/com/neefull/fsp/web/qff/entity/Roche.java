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
 * 罗氏内部发起QFF
 *
 * @Author: chengchengchu
 * @Date: 2019/11/26  20:43
 */

@TableName(value = "qff_roche")
public class Roche implements Serializable {
    private static final long serialVersionUID = 4462069592504890849L;

    /**
     *  主键id
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
     *  NO编号
     */
    @TableField("number")
    private String number;
    /**
     *  Initiator  发起人
     */
    @TableField("sponsor")
    private String sponsor;
    /**
     *  Request Date 申请日期
     */
    @TableField("req_date")
    private String reqDate;
    /**
     *  Reason 原因
     */
    @TableField("reason")
    private String reason;
    /**
     *  Product/Material Name 产品/物料名称
     */
    @TableField("mater_name")
    private String materName;
    /**
     *  Product/Material Code 产品/物料编号
     */
    @TableField("mater_code")
    private String materCode;
    /**
     *  Batch NO. / serial NO.  批号/序列号
     */
    @TableField("batch")
    private String batch;
    /**
     *  Quantity Inpacted 受影响数量
     */
    @TableField("quantity")
    private String quantity;
    /**
     * unit  单位
     */
    @TableField("unit")
    private String unit;
    /**
     *  Actions  行动
     */
    @TableField("actions")
    private String actions;
    /**
     *  Excepted Date 期望完成日期
     */
    @TableField("except_date")
    private String exceptDate;
    /**
     *  Actual Complete date 实际完成日期
     */
    @TableField("complete_date")
    private String completeDate;
    /**
     *  备注
     */
    @TableField("remark")
    private String remark;
    /**
     *  Follow_up Actions 后续行动
     */
    @TableField("follow")
    private String follow;
    /**
     *  变更记录
     */
    @TableField("alteration")
    private String alteration;

    /**
     *  状态码  1新建  2 审核中 3 完结  4异常
     */
    @TableField("status")
    private Integer status;
    /**
     *  创建日期
     */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy/MM/dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private Date createTime;


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

    /**
     *  图片
     */
    @TableField(exist = false)
    private String images;



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

    public String getSponsor() {
        return sponsor;
    }

    public void setSponsor(String sponsor) {
        this.sponsor = sponsor;
    }

    public String getReqDate() {
        return reqDate;
    }

    public void setReqDate(String reqDate) {
        this.reqDate = reqDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getMaterName() {
        return materName;
    }

    public void setMaterName(String materName) {
        this.materName = materName;
    }

    public String getMaterCode() {
        return materCode;
    }

    public void setMaterCode(String materCode) {
        this.materCode = materCode;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public String getExceptDate() {
        return exceptDate;
    }

    public void setExceptDate(String exceptDate) {
        this.exceptDate = exceptDate;
    }

    public String getCompleteDate() {
        return completeDate;
    }

    public void setCompleteDate(String completeDate) {
        this.completeDate = completeDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getFollow() {
        return follow;
    }

    public void setFollow(String follow) {
        this.follow = follow;
    }

    public String getAlteration() {
        return alteration;
    }

    public void setAlteration(String alteration) {
        this.alteration = alteration;
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

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }
}
