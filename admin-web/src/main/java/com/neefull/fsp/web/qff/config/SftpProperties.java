package com.neefull.fsp.web.qff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: chengchengchu
 * @Date: 2020/5/22  10:45
 */

@Component
@ConfigurationProperties(prefix = "qff.sftps")
public class SftpProperties {

    private String sftpPath;

    private String localPath;

    private String host;

    private String username;

    private String password;

    public String getSftpPath() {
        return sftpPath;
    }

    public void setSftpPath(String sftpPath) {
        this.sftpPath = sftpPath;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
