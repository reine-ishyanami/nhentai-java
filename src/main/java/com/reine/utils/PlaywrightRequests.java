package com.reine.utils;

import com.reine.exception.RequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 使用 Playwright 进行请求的工具类
 *
 * @author reine
 * 2024/7/17 23:55
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PlaywrightRequests {

    private final BrowserManager browserManager;

    /**
     * 试图绕过cf5s盾
     *
     * @param url 请求的url
     * @return 收到的正确响应内容
     */
    public byte[] antiCloudflare(String url) throws RequestException {
        log.debug("使用Playwright进行请求: {}", url);
        var browser = browserManager.getBrowser();
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
                log.debug("请求成功");
                return response.body();
            }
        }
        var msg = "绕过五秒盾失败";
        log.error(msg);
        throw new RequestException(0, msg);
    }

}
