package com.reine.config;

import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * 读取app.yml配置文件配置类
 *
 * @author reine
 * 2024/7/18 16:19
 */
@Getter
public class Profile {

    /**
     * 文件后缀
     */
    private final String suffix;
    /**
     * 是否压缩
     */
    private final Boolean compress;
    /**
     * 压缩密码
     */
    private final String password;
    /**
     * 语言
     */
    private final String language;
    /**
     * 压缩文件目录
     */
    private final String compressDir;
    /**
     * 下载过程中，如果文件已存在，是否替换原有的文件
     */
    private final Boolean replaceFile;
    /**
     * 下载过程重试次数
     */
    private final Integer retryTime;

    @Getter
    private static final Profile profile;

    static {
        try {
            profile = new Profile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Profile() throws IOException {
        String yamlFilePath = "app.yml";
        Yaml yaml = new Yaml();
        // 读取YAML文件
        try (InputStream inputStream = Profile.class.getClassLoader().getResourceAsStream(yamlFilePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("文件 " + yamlFilePath + " 未找到");
            }
            // 将YAML文件转换为Map对象
            Map<String, Object> yamlMap = yaml.load(inputStream);
            // 打印读取的内容
            suffix = yamlMap.get("suffix").toString();
            compress = (Boolean) yamlMap.get("compress");
            password = yamlMap.get("password").toString();
            language = yamlMap.get("language").toString();
            compressDir = yamlMap.get("compressDir").toString();
            replaceFile = (Boolean) yamlMap.get("replaceFile");
            retryTime = (Integer) yamlMap.get("retryTime");
        }
    }

}
