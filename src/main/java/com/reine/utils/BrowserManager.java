package com.reine.utils;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author reine
 * 2024/7/17 23:56
 */
@Component
public class BrowserManager {

    @Getter
    private Browser browser;

    private Playwright playwright;

    @PostConstruct
    private void startBrowser() {
        playwright = Playwright.create();
        browser = playwright.firefox().launch();
    }

    @PreDestroy
    public void closeBrowser() {
        Optional.ofNullable(browser)
                .ifPresent(Browser::close);
        Optional.ofNullable(playwright)
                .ifPresent(Playwright::close);
    }

}
