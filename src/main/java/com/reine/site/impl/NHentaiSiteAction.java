package com.reine.site.impl;

import com.reine.annotation.Timer;
import com.reine.entity.FailResult;
import com.reine.entity.HentaiDetail;
import com.reine.entity.HentaiHref;
import com.reine.entity.HentaiStore;
import com.reine.properties.Profile;
import com.reine.site.SiteAction;
import com.reine.utils.Compress;
import com.reine.utils.HttpClientRequests;
import com.reine.utils.PdfUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    @Timer
    @Override
    public HentaiDetail search(String name) {
        Pattern pattern = Pattern.compile(profile.getLanguage());
        hentaiName = name;
        final var url = "https://nhentai.net/search/?q=%s".formatted(name);
        log.info("搜索中 {}", url);
        var rsp1 = requests.requestPageContent(url);
        List<HentaiHref> hentaiHrefs = listHentaiGalleries(new String(rsp1, StandardCharsets.UTF_8));
        HentaiHref target = hentaiHrefs.stream().filter(hentaiHref -> pattern.matcher(hentaiHref.title()).find()).findFirst().orElseThrow(() -> new RuntimeException("not found"));
        var rsp2 = requests.requestPageContent(target.href());
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
        Document doc = Jsoup.parse(html);
        var resList = new ArrayList<HentaiHref>();
        for (Element gallery : doc.body().getElementsByClass("gallery")) {
            var href = gallery.getElementsByTag("a").attr("href");
            var text = gallery.getElementsByClass("caption").text();
            resList.add(new HentaiHref("https://nhentai.net%s".formatted(href), text));
        }
        return resList;
    }

    /**
     * 获取hentai详细信息
     *
     * @param html 页面内容
     * @return hentai详细信息
     */
    private HentaiDetail getHentaiDetail(String html) {
        Document doc = Jsoup.parse(html);
        var srcUrl = Objects.requireNonNull(doc.getElementById("cover"))
                .getElementsByTag("img").attr("data-src");
        String[] split = srcUrl.split("/");
        var gallery = split[split.length - 2];
        var resList = new ArrayList<String>();
        for (Element thumbs : Objects.requireNonNull(doc.getElementById("thumbnail-container"))
                .getElementsByClass("thumbs").getFirst().getElementsByClass("thumb-container")) {
            srcUrl = thumbs.getElementsByTag("img").attr("data-src");
            split = srcUrl.split("/");
            String[] img = split[split.length - 1].split("\\.");
            String first = img[0].replace("t", "");
            resList.add("%s.%s".formatted(first, img[1]));
        }
        return new HentaiDetail(gallery, resList);
    }
}
