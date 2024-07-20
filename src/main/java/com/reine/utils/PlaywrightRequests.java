package com.reine.utils;

import com.reine.exception.RequestException;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 使用 Playwright 进行请求的工具类
 *
 * @author reine
 * 2024/7/17 23:55
 */
@Slf4j
public class PlaywrightRequests {

    /**
     * 试图绕过cf5s盾
     *
     * @param url
     * @return
     */
    public static byte[] antiCloudflare(String url) throws RequestException {
        var browser = BrowserManager.getBrowser();
        try (var browserContext = browser.newContext();
             var page = browserContext.newPage()) {
            var response = page.navigate(url);
            var attempts = 0;  // 重试次数
            while (attempts < 10) {
                attempts++;
                var body = page.locator("body").textContent();
                if (body.isEmpty()) {
                    page.waitForTimeout(1000);
                    continue;
                }
                if (Objects.equals(page.title(), "Please Wait... | Cloudflare")) {
                    log.warn("疑似触发了 Cloudflare 的验证码");
                    break;
                }
                log.info("请求成功");
                return response.body();
            }
        }
        var msg = "绕过五秒盾失败";
        log.error(msg);
        throw new RequestException(0, msg);
    }

}
