package com.reine.utils;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;

import java.util.Optional;

/**
 * @author reine
 * 2024/7/17 23:56
 */
public class BrowserManager {

    private static Browser browser;

    private static Playwright playwright;

    private static Browser startBrowser() {
        playwright = Playwright.create();
        browser = playwright.firefox().launch();
        return browser;
    }

    public static Browser getBrowser() {
        return Optional.ofNullable(browser).orElse(startBrowser());
    }

    public static void closeBrowser() {
        Optional.ofNullable(browser)
                .ifPresent(Browser::close);
        Optional.ofNullable(playwright)
                .ifPresent(Playwright::close);
    }

}
