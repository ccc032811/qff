package com.neefull.fsp.web.qff.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;

import java.io.Serializable;

/**
 * @Author: chengchengchu
 * @Date: 2020/2/24  16:45
 */
@Excel("近效期QFF解析模板")
public class RecentResolver implements Serializable {


    private static final long serialVersionUID = 5674029862950781367L;
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
     *  罗氏QA处理意见
     */
    @TableField("r_conf")
    @ExcelField(value = "罗氏QA处理意见")
    private String rConf;

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

    public String getrConf() {
        return rConf;
    }

    public void setrConf(String rConf) {
        this.rConf = rConf;
    }
}
