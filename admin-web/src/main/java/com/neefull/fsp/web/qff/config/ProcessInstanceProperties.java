package com.neefull.fsp.web.qff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: chengchengchu
 * @Date: 2020/1/2  14:27
 */
@Component
@ConfigurationProperties(prefix = "qff.process")
public class ProcessInstanceProperties {

    private String commodityProcess;

    private String recentProcess;

    private String rocheProcess;

    private String imagePath;

    private String imageUrl;


    public String getCommodityProcess() {
        return commodityProcess;
    }

    public void setCommodityProcess(String commodityProcess) {
        this.commodityProcess = commodityProcess;
    }

    public String getRecentProcess() {
        return recentProcess;
    }

    public void setRecentProcess(String recentProcess) {
        this.recentProcess = recentProcess;
    }

    public String getRocheProcess() {
        return rocheProcess;
    }

    public void setRocheProcess(String rocheProcess) {
        this.rocheProcess = rocheProcess;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
