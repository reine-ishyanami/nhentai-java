package com.reine.entity;

import java.util.List;

/**
 * hentai gallery编号，及其所有页数名称
 *
 * @param gallery 编号
 * @param imgList 所有页数名称
 * @author reine
 * 2024/7/18 10:13
 */
public record HentaiDetail(String gallery, List<String> imgList) {
}
