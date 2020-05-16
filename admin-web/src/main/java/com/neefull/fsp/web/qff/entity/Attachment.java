package com.neefull.fsp.web.qff.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * @Author: chengchengchu
 * @Date: 2020/5/12  17:02
 */
@TableName(value = "qff_attachment")
public class Attachment implements Serializable {

    private static final long serialVersionUID = -6743683518722437044L;
    /**
     * 主键id
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
     * 关联QFF id
     */
    @TableField("qff_id")
    private String qffId;
    /**
     * 关联QFF类型
     */
    @TableField("qff_type")
    private String qffType;
    /**
     * 文件类型
     */
    @TableField("attach_type")
    private String attachType;
    /**
     * 文件大小
     */
    @TableField("attach_size")
    private Long attachSize;
    /**
     * 文件路径
     */
    @TableField("attach_path")
    private String attachPath;
    /**
     * 文件名
     */
    @TableField("remark")
    private String remark;
    /**
     * 文件来源   1sap获取的文件   2 本系统上传的文件
     */
    @TableField("source")
    private Integer source;
    /**
     * 区分是谁上传的
     */
    @TableField("vest")
    private String vest;
    /**
     * 是否可用   0 不可以  1 可用
     */
    @TableField("enable")
    private Integer enable;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getQffId() {
        return qffId;
    }

    public void setQffId(String qffId) {
        this.qffId = qffId;
    }

    public String getQffType() {
        return qffType;
    }

    public void setQffType(String qffType) {
        this.qffType = qffType;
    }

    public String getAttachType() {
        return attachType;
    }

    public void setAttachType(String attachType) {
        this.attachType = attachType;
    }

    public Long getAttachSize() {
        return attachSize;
    }

    public void setAttachSize(Long attachSize) {
        this.attachSize = attachSize;
    }

    public String getAttachPath() {
        return attachPath;
    }

    public void setAttachPath(String attachPath) {
        this.attachPath = attachPath;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public String getVest() {
        return vest;
    }

    public void setVest(String vest) {
        this.vest = vest;
    }

    public Integer getEnable() {
        return enable;
    }

    public void setEnable(Integer enable) {
        this.enable = enable;
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



}
