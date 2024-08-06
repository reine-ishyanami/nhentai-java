package com.reine.utils;

import com.reine.entity.FailResult;
import com.reine.entity.HentaiStore;
import com.reine.properties.Profile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 使用 HttpClient 进行请求的工具类
 *
 * @author reine
 * 2024/7/18 15:25
 */
@RequiredArgsConstructor
@Slf4j
public class HttpClientRequests {

    private final HttpClient client;

    @Setter
    private Profile profile;

    public HttpClientRequests() {
        client = httpClientWithoutSSL();
    }

    /**
     * 创建一个关闭SSL的httpclient
     *
     * @return
     */
    private HttpClient httpClientWithoutSSL() {
        try {
            // 创建一个TrustManager，信任所有证书
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            // 设置SSL上下文
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            Executor executor = Executors.newVirtualThreadPerTaskExecutor();
            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .executor(executor).build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    @Getter
    private final List<FailResult> failList = new ArrayList<>();

    /**
     * 异步下载文件
     *
     * @param hentaiStore
     * @return
     */
    public CompletableFuture<Void> downloadImage(HentaiStore hentaiStore, int retryCount) {
        log.debug("开始下载文件: {}", hentaiStore.url());
        var url = hentaiStore.url();
        var path = hentaiStore.path();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                .GET()
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenAccept(rsp -> {
                    int code = rsp.statusCode();
                    // 递归重试
                    if (code != 200 && retryCount < profile.getRetryTime()) {
                        log.debug("重试 {} 次, {}", retryCount + 1, url);
                        downloadImage(hentaiStore, retryCount + 1);
                        return;
                    }
                    File file = path.toFile();
                    // 重试次数超过5次
                    if (code != 200 && retryCount == profile.getRetryTime()) {
                        failList.add(new FailResult(file.getName(), "重试次数过多", Level.ERROR));
                        return;
                    }
                    try {
                        if (file.createNewFile()) {
                            writeBytesToFile(file, rsp.body());
                        } else if (profile.getReplaceFile()) {
                            if (file.delete() && file.createNewFile()) {
                                writeBytesToFile(file, rsp.body());
                            } else {
                                failList.add(new FailResult(file.getName(), "文件创建失败", Level.ERROR));
                            }
                        } else {
                            failList.add(new FailResult(file.getName(), "文件已存在", Level.WARN));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * 向文件中写入二进制信息
     *
     * @param file  文件
     * @param bytes 二进制信息
     * @throws IOException 读写异常
     */
    private void writeBytesToFile(File file, byte[] bytes) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
        }
    }
}
