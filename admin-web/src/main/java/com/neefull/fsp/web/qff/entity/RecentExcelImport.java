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

import java.io.Serializable;
import java.util.Date;

/**用于近效期excel导出
 * @Author: chengchengchu
 * @Date: 2020/1/9  9:48
 */

@TableName(value = "qff_recent")
@Excel("近效期QFF")
public class RecentExcelImport implements Serializable {

    private static final long serialVersionUID = 302033785722298807L;
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
     *  康德乐物料号
     */
    @TableField("k_mater")
    @ExcelField(value = "康德乐物料号")
    private String kMater;
    /**
     *  罗氏物料号
     */
    @TableField("r_mater")
    @ExcelField(value = "罗氏物料号")
    private String rMater;
    /**
     *  产品名称
     */
    @TableField("name")
    @ExcelField(value = "产品名称")
    private String name;
    /**
     *  有效期
     */
    @TableField("use_life")
    @ExcelField(value = "有效期")
    private String useLife;
    /**
     *  批号
     */
    @TableField("batch")
    @ExcelField(value = "批号")
    private String batch;
    /**
     *  SAP 批次
     */
    @TableField("sap_batch")
    @ExcelField(value = "SAP批次")
    private String sapBatch;
    /**
     *  工厂
     */
    @TableField("factory")
    @ExcelField(value = "工厂")
    private String factory;
    /**
     *  库位
     */
    @TableField("ware_house")
    @ExcelField(value = "库位")
    private String wareHouse;
    /**
     *  数量
     */
    @TableField("number")
    @ExcelField(value = "数量")
    private String number;
    /**
     * unit  单位
     */
    @TableField("unit")
    @ExcelField(value = "单位")
    private String unit;
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
    @JsonFormat(pattern = "yyyy/MM/dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private Date createTime;
    /**
     *  更新日期
     */
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy/MM/dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private Date updateTime;




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

    public String getkMater() {
        return kMater;
    }

    public void setkMater(String kMater) {
        this.kMater = kMater;
    }

    public String getrMater() {
        return rMater;
    }

    public void setrMater(String rMater) {
        this.rMater = rMater;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUseLife() {
        return useLife;
    }

    public void setUseLife(String useLife) {
        this.useLife = useLife;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getSapBatch() {
        return sapBatch;
    }

    public void setSapBatch(String sapBatch) {
        this.sapBatch = sapBatch;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public String getWareHouse() {
        return wareHouse;
    }

    public void setWareHouse(String wareHouse) {
        this.wareHouse = wareHouse;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
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
}
