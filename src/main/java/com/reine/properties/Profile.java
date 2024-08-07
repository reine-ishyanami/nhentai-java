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
     * 打包相关参数
     */
    private final Compress compress = new Compress();

    /**
     * 生成 pdf 相关参数
     */
    private final Pdf pdf = new Pdf();

    /**
     * 文件后缀 TODO
     */
    private String suffix = "jpg";
    /**
     * 语言
     */
    private String language = "Chinese";
    /**
     * 下载过程中，如果文件已存在，是否替换原有的文件
     */
    private Boolean replaceFile = false;
    /**
     * 下载过程重试次数
     */
    private Integer retryTime = 5;
    /**
     * 下载根路径
     */
    private String rootDir = ".";

    @Data
    public static class Compress{
        /**
         * 是否开启
         */
        private Boolean enable = false;
        /**
         * 压缩密码，为空即不进行加密
         */
        private String password = "";
        /**
         * 压缩文件目录
         */
        private String dir = "cpr";
        /**
         * 加密方法
         * <p>
         * 1:ZIP_STANDARD</p><p>
         * 2:ZIP_STANDARD_VARIANT_STRONG</p><p>
         * 3:AES_128</p><p>
         * 4:AES_192</p><p>
         * 5:AES_256</p>
         */
        private Byte encryptionMethod = 3;
        /**
         * 压缩文件分片大小，单位MB，0表示不分片
         */
        private Integer splitSize = 0;
        /**
         * 压缩等级
         * <p>
         * Level 0 无压缩
         * </p>
         * </p>
         * Level 1 最快的压缩速度。
         * </p>
         * <p>
         * Level 2
         * </p>
         * <p>
         * Level 3
         * <p>
         * Level 5 速度和压缩率别之间的折衷。
         * </p>
         * <p>
         * Level 6
         * </p>
         * <p>
         * Level 7
         * </p>
         * <p>
         * Level 8
         * </p>
         * <p>
         * Level 9 最高的压缩率。
         * </p>
         */
        private Integer level = 5;
        /**
         *  是否开启压缩进度条
         */
        private Boolean progressVisible = false;
    }

    @Data
    public static class Pdf{
        /**
         * 存放 pdf 的路径
         */
        private String dir = "pdf";
        /**
         * 是否转换成 PDF
         */
        private Boolean enable = false;
    }
}
