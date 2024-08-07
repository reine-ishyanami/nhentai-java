package com.reine.site.impl;

import com.microsoft.playwright.Locator;
import com.reine.annotation.Timer;
import com.reine.entity.FailResult;
import com.reine.entity.HentaiDetail;
import com.reine.entity.HentaiHref;
import com.reine.entity.HentaiStore;
import com.reine.properties.Profile;
import com.reine.site.SiteAction;
import com.reine.utils.*;
import java.io.File;
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

    private final PdfUtils pdfUtils;

    private HentaiDetail hentaiDetail;

    private String hentaiName;

    @Getter
    private List<FailResult> failList = new ArrayList<>();

    private final Compress compress;

    @Override
    public String baseUrl() {
        return "https://i3.nhentai.net/galleries";
    }

    /**
     * 获取随机的结果排序方式
     *
     * @return 随机的结果排序方式
     */
    String getSoft() {
        String[] ex = {"&sort=popular", "&sort=popular-today", ""};
        return ex[RandomIndexUtils.getRandomIndex(ex.length)];
    }
    /**
     * 搜索hentai
     *
     * @param name     搜索关键词或标签，如果是画廊id就直接访问详情
     * @param isRandom 是否随机返回搜索结果
     * @return 搜索结果
     */
    @Timer
    @Override
    public HentaiDetail search(String name, Boolean isRandom) {
        Pattern pattern = Pattern.compile(profile.getLanguage());
        hentaiName = name;
        var isOnlyId = name.matches("^[0-9]+$");
        final var url = isRandom ? "https://nhentai.net/search/?q=%s+%s".formatted(
            name.equals("null") ? " " : name.replace(" ", "+"), getSoft())

            : "https://nhentai.net/search/?q=%s".formatted(name);
        log.info("搜索中 {}", url);
        var rsp1 = playwright.antiCloudflare(url);
        byte[] rsp2;
        if (!isOnlyId) {
            List<HentaiHref> hentaiHrefs = listHentaiGalleries(new String(rsp1, StandardCharsets.UTF_8));
            var result = hentaiHrefs.stream()
                .filter(hentaiHref -> pattern.matcher(hentaiHref.title()).find()).toList();
            HentaiHref target;
            if (result.isEmpty()) {
                try {
                    throw new Exception("未找到任何符合条件的作品");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                target = isRandom ? getRandomItem(result) : result.get(0);
            }
            rsp2 = playwright.antiCloudflare(target.href());
        } else {
            rsp2 = rsp1;
        }
        hentaiDetail = getHentaiDetail(new String(rsp2, StandardCharsets.UTF_8));
        return hentaiDetail;
    }

    /**
     * 用于从 list 里随机抽一个
     *
     * @param list 被抽的list
     * @return 抽中项
     */
    private <T> T getRandomItem(List<T> list) {
        return list.get(RandomIndexUtils.getRandomIndex(list.size()));
    }

    @Timer
    @Override
    public HentaiDetail search(String name) {
        Pattern pattern = Pattern.compile(profile.getLanguage());
        hentaiName = name;
        final var url = "https://nhentai.net/search/?q=%s".formatted(name);
        log.info("搜索中 {}", url);
        var rsp1 = playwright.antiCloudflare(url);
        List<HentaiHref> hentaiHrefs = listHentaiGalleries(new String(rsp1, StandardCharsets.UTF_8));
        HentaiHref target = hentaiHrefs.stream().filter(hentaiHref -> pattern.matcher(hentaiHref.title()).find()).findFirst().orElseThrow(() -> new RuntimeException("not found"));
        var rsp2 = playwright.antiCloudflare(target.href());
        hentaiDetail = getHentaiDetail(new String(rsp2, StandardCharsets.UTF_8));
        return hentaiDetail;
    }

    private Path hentaiPath;

    @Timer
    @Override
    public List<FailResult> download() {
        hentaiPath = Path.of(profile.getRootDir(), hentaiName);
        if (!hentaiPath.toFile().mkdirs()) {
            log.warn("目录 {} 已存在。", hentaiName);
        }
        log.info("开始下载");
        List<CompletableFuture<Void>> futures = hentaiDetail.imgList()
                .stream()
                .map(img -> new HentaiStore("%s/%s/%s".formatted(baseUrl(), hentaiDetail.gallery(), img),
                        Path.of(profile.getRootDir(), hentaiName, img)))
                .map(img -> requests.downloadImage(img, 0)).toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
        log.info("下载完成");
        failList = requests.getFailList();
        return failList;
    }

    @Timer
    @Override
    public boolean compress() throws IOException {
        return compress.packageToZip(hentaiName);
    }


    @Timer
    @Override
    public boolean convertToPdf(Boolean overwrite) throws IOException {
        return pdfUtils.convertToPdf(hentaiPath, Path.of(profile.getPdf().getDir(), hentaiName + ".pdf"), overwrite);
    }

    /**
     * 展示搜索出来的hentai列表
     *
     * @param html 页面内容
     * @return hentai列表
     */
    private List<HentaiHref> listHentaiGalleries(String html) {
        var browser = browserManager.getBrowser();
        try (var browserContext = browser.newContext(); var page = browserContext.newPage()) {
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
        try (var browserContext = browser.newContext(); var page = browserContext.newPage()) {
            page.setContent(html);
            var srcUrl = page.locator("#cover").locator("img").getAttribute("data-src");
            String[] split = srcUrl.split("/");
            var gallery = split[split.length - 2];
            var resList = new ArrayList<String>();
            for (Locator div : page.locator("#thumbnail-container").locator(".thumbs").locator(".thumb-container").all()) {
                srcUrl = div.locator("img").getAttribute("data-src");
                split = srcUrl.split("/");
                String[] img = split[split.length - 1].split("\\.");
                String first = img[0].replace("t", "");
                resList.add("%s.%s".formatted(first, img[1]));
            }

            List<String> title = new ArrayList<>();
            for (Locator a : page.locator("#info > h1 > span").all()) {
                title.add(a.innerText());
            }
            List<String> tags = new ArrayList<>();
            for (Locator a : page.locator("#tags").locator("div:nth-child(3)").locator("span")
                .locator("a.tag.tag").all()
            ) {
                tags.add(a.locator("span.name").innerText());
            }
            List<String> lang = new ArrayList<>();
            for (Locator a : page.locator("#tags > div:nth-child(6) > span > a.tag").all()) {
                lang.add(a.innerText());
            }
            String g = page.locator("#gallery_id").innerText().replace("#", "");
            return new HentaiDetail(title, gallery, resList, tags, lang, g);
        }
    }

    /**
     * 获取所有标签
     *
     * @return 标签数组
     */
    @Override
    public List<String> getLabels() {
        return hentaiDetail.tags();
    }

    /**
     * 获取封面
     *
     * @return 封面 url
     */
    @Override
    public String getCover() {
        return "%s/%s/%s".formatted(baseUrl(), hentaiDetail.gallery(), hentaiDetail.imgList().get(0));
    }

    /**
     * 获取唯一的画廊id
     */
    @Override
    public String getGalleryId() {
        return hentaiDetail.g();
    }

    /**
     * 获取语言
     *
     * @return 语言
     */
    @Override
    public List<String> getLang() {
        return hentaiDetail.languages();
    }

    /**
     * 获取 hentai 以 zip 形式返回
     *
     * @param id       画廊 id
     * @param password 密码
     * @return zip file
     * @throws IOException io 异常
     */
    @Override
    public File getZip(String id, String password) throws IOException {
        search(id, false);
        download();
        compress.packageToZip(id, password);
        return compress.getCurrentFile();
    }

    /**
     * 获取 hentai 以 pdf 形式返回
     *
     * @param id       画廊id
     * @param password 密码 //TODO pdf加密暂未实现
     * @return pdf file
     * @throws IOException io 异常
     */
    @Override
    public File getPdf(String id, String password) throws IOException {
        search(id, false);
        download();
        pdfUtils.convertToPdf(hentaiPath,
            Path.of(profile.getPdf().getDir(), hentaiName + ".pdf"), false);
        return pdfUtils.getCurrentPdf();
    }

}
