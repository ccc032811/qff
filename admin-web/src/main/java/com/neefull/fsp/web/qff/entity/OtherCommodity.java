package com.neefull.fsp.web.qff.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.wuwenze.poi.annotation.Excel;
import com.wuwenze.poi.annotation.ExcelField;

import java.io.Serializable;

/**其他手工模板
 * @Author: chengchengchu
 * @Date: 2020/9/11  17:02
 */

@Excel("Sheet")
public class OtherCommodity implements Serializable {

    private static final long serialVersionUID = 5803403681193307578L;

    /**
     *  QFF编号
     */
    @ExcelField(value = "QFF编号")
    private String number;
    /**
     *  Plant工厂
     */
    @ExcelField(value = "Plant工厂")
    private String plant;
    /**
     *  KDL Material 物料
     */
    @ExcelField(value = "KDLMaterial物料")
    private String kMater;
    /**
     *  KDL SAP Batch 康德乐SAP批次
     */
    @ExcelField(value = "康德乐SAP批次")
    private String kBatch;
    /**
     *  RD Material 罗氏物料号
     */
    @ExcelField(value = "罗氏物料号")
    private String rMater;
    /**
     *  RD Batch 罗氏批号
     */
    @ExcelField(value = "罗氏批号")
    private String rBatch;
    /**
     *  Date of Manufacturing 生产日期
     */
    @ExcelField(value = "生产日期")
    private String manuDate;
    /**
     *  Expiry Date 有效期
     */
    @ExcelField(value = "有效期")
    private String expiryDate;
    /**
     *  Quarantine 异常总数
     */
    @ExcelField(value = "异常总数")
    private String quarantine;
    /**
     *  Remark箱号/备注
     */
    @ExcelField(value = "Remark箱号/备注")
    private String getRemark;
    /**
     *  QFF 上报阶段
     */
    @ExcelField(value = "上报阶段")
    private String type;
    /**
     *  采购来源
     */
    @ExcelField(value = "采购来源")
    private String source;
    /**
     *  产品分类
     */
    @ExcelField(value = "产品分类")
    private String classify;
    /**
     *  变更记录
     */
    @ExcelField(value = "变更记录")
    private String alteration;
    /**
     *  备注
     */
    @TableField("remark")
    @ExcelField(value = "备注")
    private String remark;
    /**
     *  Time of repley 回复日期
     */
    @ExcelField(value = "回复日期")
    private String repTime;
    /**
     *  投诉编号
     */
    @ExcelField(value = "投诉编号")
    private String compNumber;
    /**
     *  QFF 退货原因
     */
    @ExcelField(value = "QFF原因")
    private String reason;
    /**
     *  RD QA confirmation 罗氏QA处理意见
     */
    @ExcelField(value = "罗氏QA处理意见")
    private String rConf;
    /**
     *  仪器工程师检查结果
     */
    @ExcelField(value = "仪器工程师检查结果")
    private String checkResult;


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

    public String getGetRemark() {
        return getRemark;
    }

    public void setGetRemark(String getRemark) {
        this.getRemark = getRemark;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRepTime() {
        return repTime;
    }

    public void setRepTime(String repTime) {
        this.repTime = repTime;
    }

    public String getCompNumber() {
        return compNumber;
    }

    public void setCompNumber(String compNumber) {
        this.compNumber = compNumber;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getrConf() {
        return rConf;
    }

    public void setrConf(String rConf) {
        this.rConf = rConf;
    }

    public String getCheckResult() {
        return checkResult;
    }

    public void setCheckResult(String checkResult) {
        this.checkResult = checkResult;
    }
}
