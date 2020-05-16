//package com.neefull.fsp.web.qff.entity;
//
//import com.baomidou.mybatisplus.annotation.IdType;
//import com.baomidou.mybatisplus.annotation.TableField;
//import com.baomidou.mybatisplus.annotation.TableId;
//import com.baomidou.mybatisplus.annotation.TableName;
//
//import java.io.Serializable;
//import java.util.Date;
//
///**图片
// * @Author: chengchengchu
// * @Date: 2019/12/26  18:21
// */
//@TableName(value = "qff_image")
//public class DateImage implements Serializable {
//
//    private static final long serialVersionUID = -2168221591077896448L;
//    /**
//     *  主键id
//     */
//    @TableId(value = "id",type = IdType.AUTO)
//    private Integer id;
//    /**
//     *  关联的数据id
//     */
//    @TableField("data_id")
//    private Integer dataId;
//    /**
//     *  关联数据库表的名字
//     */
//    @TableField("relevance")
//    private String relevance;
//    /**
//     *  来源  1  SAP 获取的图片    2  本系统上传的图片
//     */
//    @TableField("source")
//    private Integer source;
//    /**
//     *  归属  区分是谁上传的
//     */
//    @TableField("vest")
//    private String vest;
//    /**
//     *  图片
//     */
//    @TableField("image")
//    private String image;
//    /**
//     *  创建时间
//     */
//    @TableField("create_time")
//    private Date createTime;
//    /**
//     *  更新时间
//     */
//    @TableField("update_time")
//    private Date updateTime;
//
//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(Integer id) {
//        this.id = id;
//    }
//
//    public Integer getDataId() {
//        return dataId;
//    }
//
//    public void setDataId(Integer dataId) {
//        this.dataId = dataId;
//    }
//
//    public String getRelevance() {
//        return relevance;
//    }
//
//    public void setRelevance(String relevance) {
//        this.relevance = relevance;
//    }
//
//    public Integer getSource() {
//        return source;
//    }
//
//    public void setSource(Integer source) {
//        this.source = source;
//    }
//
//    public String getVest() {
//        return vest;
//    }
//
//    public void setVest(String vest) {
//        this.vest = vest;
//    }
//
//    public String getImage() {
//        return image;
//    }
//
//    public void setImage(String image) {
//        this.image = image;
//    }
//
//    public Date getCreateTime() {
//        return createTime;
//    }
//
//    public void setCreateTime(Date createTime) {
//        this.createTime = createTime;
//    }
//
//    public Date getUpdateTime() {
//        return updateTime;
//    }
//
//    public void setUpdateTime(Date updateTime) {
//        this.updateTime = updateTime;
//    }
//}
