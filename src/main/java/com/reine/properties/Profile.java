package com.reine.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 读取app.yml配置文件配置类
 *
 * @author reine
 * 2024/7/18 16:19
 */
@Data
@ConfigurationProperties(prefix = "download.config")
public class Profile {

    /**
     * 文件后缀
     */
    private String suffix;
    /**
     * 是否压缩
     */
    private Boolean compress;
    /**
     * 压缩密码
     */
    private String password;
    /**
     * 语言
     */
    private String language;
    /**
     * 压缩文件目录
     */
    private String compressDir;
    /**
     * 下载过程中，如果文件已存在，是否替换原有的文件
     */
    private Boolean replaceFile;
    /**
     * 下载过程重试次数
     */
    private Integer retryTime;

}
