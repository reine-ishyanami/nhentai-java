package com.reine.site.impl;

import com.microsoft.playwright.Locator;
import com.reine.annotation.Timer;
import com.reine.properties.Profile;
import com.reine.entity.FailResult;
import com.reine.entity.HentaiDetail;
import com.reine.entity.HentaiHref;
import com.reine.entity.HentaiStore;
import com.reine.site.SiteAction;
import com.reine.utils.BrowserManager;
import com.reine.utils.HttpClientRequests;
import com.reine.utils.PlaywrightRequests;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;


/**
 * <a href="https://nhentai.net">...</a> 网站爬虫
 *
 * @author reine
 * 2024/7/20 11:05
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NHentaiSiteAction implements SiteAction {

    private final Profile profile;

    private final HttpClientRequests requests;

    private final PlaywrightRequests playwright;

    private final BrowserManager browserManager;


    @Override
    public String baseUrl() {
        return "https://i3.nhentai.net/galleries";
    }

    private HentaiDetail hentaiDetail;

    private String hentaiName;

    @Timer
    @Override
    public HentaiDetail search(String name) {
        Pattern pattern = Pattern.compile(profile.getLanguage());
        hentaiName = name;
        final var url = "https://nhentai.net/search/?q=%s".formatted(name);
        log.info("搜索中 {}", url);
        var rsp1 = playwright.antiCloudflare(url);
        List<HentaiHref> hentaiHrefs = listHentaiGalleries(new String(rsp1, StandardCharsets.UTF_8));
        HentaiHref target = hentaiHrefs.stream()
                .filter(hentaiHref -> pattern.matcher(hentaiHref.title()).find())
                .findFirst().orElseThrow(() -> new RuntimeException("not found"));
        var rsp2 = playwright.antiCloudflare(target.href());
        hentaiDetail = getHentaiDetail(new String(rsp2, StandardCharsets.UTF_8));
        return hentaiDetail;
    }

    @Getter
    private List<FailResult> failList = new ArrayList<>();

    @Timer
    @Override
    public List<FailResult> download() {
        if (!Path.of(hentaiName).toFile().mkdirs()) {
            log.error("目录 {} 已存在。", hentaiName);
        }
        List<CompletableFuture<Void>> futures = hentaiDetail.imgList().stream()
                .map(img -> new HentaiStore("%s/%s/%s".formatted(baseUrl(), hentaiDetail.gallery(), img), Path.of(hentaiName, img)))
                .map(img -> requests.downloadImage(img, 0)).toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
        log.info("下载完成");
        failList = requests.getFailList();
        return failList;
    }

    @Override
    public boolean packageTo7z() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * 展示搜索出来的hentai列表
     *
     * @param html 页面内容
     * @return hentai列表
     */
    private List<HentaiHref> listHentaiGalleries(String html) {
        var browser = browserManager.getBrowser();
        try (var browserContext = browser.newContext();
             var page = browserContext.newPage()) {
            page.setContent(html);
            var resList = new ArrayList<HentaiHref>();
            for (Locator locator : page.locator(".gallery").all()) {
                var href = locator.locator("a").getAttribute("href");
                var text = locator.locator(".caption").innerText();
                resList.add(new HentaiHref("https://nhentai.net%s".formatted(href), text));
            }
            return resList;
        }
    }

    /**
     * 获取hentai详细信息
     *
     * @param html 页面内容
     * @return hentai详细信息
     */
    private HentaiDetail getHentaiDetail(String html) {
        var browser = browserManager.getBrowser();
        try (var browserContext = browser.newContext();
             var page = browserContext.newPage()) {
            page.setContent(html);
            var srcUrl = page.locator("#cover").locator("img")
                    .getAttribute("data-src");
            String[] split = srcUrl.split("/");
            var gallery = split[split.length - 2];
            var resList = new ArrayList<String>();
            for (Locator div : page.locator("#thumbnail-container")
                    .locator(".thumbs")
                    .locator(".thumb-container")
                    .all()) {
                srcUrl = div.locator("img").getAttribute("data-src");
                split = srcUrl.split("/");
                String[] img = split[split.length - 1].split("\\.");
                String first = img[0].replace("t", "");
                resList.add("%s.%s".formatted(first, img[1]));
            }
            return new HentaiDetail(gallery, resList);
        }
    }
}
