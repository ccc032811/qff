package com.neefull.fsp.web.qff.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author: chengchengchu
 * @Date: 2019/12/6  17:24
 */

@TableName(value = "qff_commodity")
@Excel("Sheet")
public class Commodity implements Serializable {


    private static final long serialVersionUID = -3702719768627092566L;

    /**
     *  主键id
     */
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
     *  运输单号
     */
    @TableField("transport")
    @ExcelField(value = "运输单号")
    private String transport;
    /**
     *  QFF编号
     */
    @TableField("number")
    @ExcelField(value = "QFF编号")
    private String number;
    /**
     *  Plant工厂
     */
    @TableField("plant")
    @ExcelField(value = "Plant工厂")
    private String plant;
    /**
     *  KDL Material 物料
     */
    @TableField("k_mater")
    @ExcelField(value = "KDLMaterial物料")
    private String kMater;
    /**
     *  KDL SAP Batch 康德乐SAP批次
     */
    @TableField("k_batch")
    @ExcelField(value = "康德乐SAP批次")
    private String kBatch;
    /**
     *  RD Material 罗氏物料号
     */
    @TableField("r_mater")
    @ExcelField(value = "罗氏物料号")
    private String rMater;
    /**
     *  是否是危险品   01危险
     */
    @TableField("is_danger")
    @ExcelField(value = "是否是危险品")
    private String isDanger;
    /**
     *  Principal Material 药厂物料号
     */
    @TableField("p_mater")
    @ExcelField(value = "物料描述")
    private String pMater;
    /**
     *  RD Batch 罗氏批号
     */
    @TableField("r_batch")
    @ExcelField(value = "罗氏批号")
    private String rBatch;
    /**
     *  Date of Manufacturing 生产日期
     */
    @TableField("manu_date")
    @ExcelField(value = "生产日期")
    private String manuDate;
    /**
     *  Expiry Date 有效期
     */
    @TableField("expiry_date")
    @ExcelField(value = "有效期")
    private String expiryDate;
    /**
     *  Quarantine 异常总数
     */
    @TableField("quarantine")
    @ExcelField(value = "异常总数")
    private String quarantine;
    /**
     * unit  康德乐单位
     */
    @TableField("k_unit")
    private String kUnit;
    /**
     * unit  罗氏单位
     */
    @TableField("r_unit")
    @ExcelField(value = "单位")
    private String rUnit;
    /**
     *  BA
     */
    @TableField("ba")
    @ExcelField(value = "BA")
    private String ba;
    /**
     *  QFF 上报阶段
     */
    @TableField("stage")
    @ExcelField(value = "上报阶段")
    private String stage;
    /**
     *  采购来源
     */
    @TableField("source")
    @ExcelField(value = "采购来源")
    private String source;
    /**
     *  注册证号
     */
    @TableField("register")
    @ExcelField(value = "注册证号")
    private String register;
    /**
     *  产品分类
     */
    @TableField("classify")
    @ExcelField(value = "产品分类")
    private String classify;
    /**
     *  Remark箱号/备注
     */
    @TableField("get_remark")
    @ExcelField(value = "Remark箱号/备注")
    private String getRemark;
    /**
     *  QFF 发起日期
     */
    @TableField("init_date")
    @ExcelField(value = "发起日期")
    private String initDate;
    /**
     *  Time of repley 回复日期
     */
    @TableField("rep_time")
    @ExcelField(value = "回复日期")
    private String repTime;
    /**
     *  投诉编号
     */
    @TableField("comp_number")
    @ExcelField(value = "投诉编号")
    private String compNumber;
    /**
     *  QFF 退货原因
     */
    @TableField("reason")
    @ExcelField(value = "QFF原因")
    private String reason;
    /**
     *  RD QA confirmation 罗氏QA处理意见
     */
    @TableField("r_conf")
    @ExcelField(value = "罗氏QA处理意见")
    private String rConf;
    /**
     *  仪器工程师检查结果
     */
    @TableField("check_result")
    @ExcelField(value = "仪器工程师检查结果")
    private String checkResult;
    /**
     *  备注
     */
    @TableField("remark")
    @ExcelField(value = "备注")
    private String remark;
    /**
     *  系统接收的类型
     */
    @TableField("type")
    private String type;
    /**
     *  变更记录
     */
    @TableField("alteration")
    @ExcelField(value = "变更记录")
    private String alteration;
    /**
     *  状态码  1新建  2 审核中 3 完结  4异常
     */
    @TableField("status")
    private Integer status;
    /**
     * 是否存在附件  0不存在   1存在
     */
    @TableField("accessory")
    private Integer accessory;
    /**
     *  创建日期
     */
    @ExcelField(value = "创建时间",dateFormat = "yyyy年MM月dd日")
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
     * 是否能审核  0 表示可以
     */
    @TableField(exist = false)
    private Integer isAllow;
    /**
     *  图片
     */
    @TableField(exist = false)
    private String images;
    /**
     * 是否可审核   1  可审核  用于接收前端页面查询条件
     */
    @TableField(exist = false)
    private Integer att;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPlant() {
        return plant;
    }

    public void setPlant(String plant) {
        this.plant = plant;
    }

    public String getkMater() {
        return kMater;
    }

    public void setkMater(String kMater) {
        this.kMater = kMater;
    }

    public String getkBatch() {
        return kBatch;
    }

    public void setkBatch(String kBatch) {
        this.kBatch = kBatch;
    }

    public String getrMater() {
        return rMater;
    }

    public void setrMater(String rMater) {
        this.rMater = rMater;
    }

    public String getIsDanger() {
        return isDanger;
    }

    public void setIsDanger(String isDanger) {
        this.isDanger = isDanger;
    }

    public String getpMater() {
        return pMater;
    }

    public void setpMater(String pMater) {
        this.pMater = pMater;
    }

    public String getrBatch() {
        return rBatch;
    }

    public void setrBatch(String rBatch) {
        this.rBatch = rBatch;
    }

    public String getManuDate() {
        return manuDate;
    }

    public void setManuDate(String manuDate) {
        this.manuDate = manuDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getQuarantine() {
        return quarantine;
    }

    public void setQuarantine(String quarantine) {
        this.quarantine = quarantine;
    }

    public String getkUnit() {
        return kUnit;
    }

    public void setkUnit(String kUnit) {
        this.kUnit = kUnit;
    }

    public String getrUnit() {
        return rUnit;
    }

    public void setrUnit(String rUnit) {
        this.rUnit = rUnit;
    }

    public String getGetRemark() {
        return getRemark;
    }

    public void setGetRemark(String getRemark) {
        this.getRemark = getRemark;
    }

    public String getInitDate() {
        return initDate;
    }

    public void setInitDate(String initDate) {
        this.initDate = initDate;
    }

    public String getrConf() {
        return rConf;
    }

    public void setrConf(String rConf) {
        this.rConf = rConf;
    }

    public String getRepTime() {
        return repTime;
    }

    public void setRepTime(String repTime) {
        this.repTime = repTime;
    }

    public String getCheckResult() {
        return checkResult;
    }

    public void setCheckResult(String checkResult) {
        this.checkResult = checkResult;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCompNumber() {
        return compNumber;
    }

    public void setCompNumber(String compNumber) {
        this.compNumber = compNumber;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getBa() {
        return ba;
    }

    public void setBa(String ba) {
        this.ba = ba;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRegister() {
        return register;
    }

    public void setRegister(String register) {
        this.register = register;
    }

    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify;
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

    public Integer getAccessory() {
        return accessory;
    }

    public void setAccessory(Integer accessory) {
        this.accessory = accessory;
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

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Integer getAtt() {
        return att;
    }

    public void setAtt(Integer att) {
        this.att = att;
    }
}
