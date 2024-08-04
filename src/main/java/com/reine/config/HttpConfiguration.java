package com.reine.config;

import com.reine.properties.Profile;
import com.reine.utils.HttpClientRequests;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author reine
 * 2024/8/4 0:02
 */
@Configuration
@EnableConfigurationProperties(Profile.class)
@RequiredArgsConstructor
public class HttpConfiguration {

    private final Profile profile;

    @Bean
    public HttpClientRequests httpClientRequests() {
        HttpClientRequests httpClientRequests = new HttpClientRequests();
        httpClientRequests.setProfile(profile);
        return httpClientRequests;
    }
}
