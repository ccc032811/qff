package com.neefull.common.core.oss.config;

import com.neefull.common.core.oss.OssManager;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "qiniu")
@PropertySource(value = "classpath:qiniu.properties", ignoreResourceNotFound = true, encoding = "UTF-8")
public class QiniuConfig {

    private String accessKey;
    private String secretKey;
    private String bucket;

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }
    @Bean
    public OssManager getOssManager() {
        return new OssManager();
    }
}
