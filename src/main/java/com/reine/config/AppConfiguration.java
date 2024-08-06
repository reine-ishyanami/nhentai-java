package com.reine.config;

import com.reine.properties.Profile;
import com.reine.utils.HttpClientRequests;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author reine
 * 2024/8/4 0:02
 */
@Configuration
@EnableConfigurationProperties(Profile.class)
@RequiredArgsConstructor
public class AppConfiguration {

    private final Profile profile;

    @Bean
    public HttpClientRequests httpClientRequests() {
        HttpClientRequests httpClientRequests = new HttpClientRequests();
        httpClientRequests.setProfile(profile);
        return httpClientRequests;
    }

    @PostConstruct
    public void init() {
        try {
            // 创建 pdf 文件夹
            Files.createDirectories(Paths.get(profile.getPdf().getPdfDir()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
