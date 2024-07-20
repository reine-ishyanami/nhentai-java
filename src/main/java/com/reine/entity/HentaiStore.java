package com.reine.entity;

import java.nio.file.Path;

/**
 * hentai单页下载URL及其指定下载到本地的路径
 *
 * @param url  下载url
 * @param path 下载到本地的路径（相对路径）
 * @author reine
 * 2024/7/18 11:05
 */
public record HentaiStore(String url, Path path) {
}
