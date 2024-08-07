package com.reine.entity;

import java.util.List;

/**
 * hentai gallery编号，及其所有页数名称
 *
 * @param gallery 编号，不一定能够用来访问
 * @param imgList 所有页数名称
 * @param g       可以用来直接访问的编号
 * @author reine 2024/7/18 10:13
 */
public record HentaiDetail(List<String> title, String gallery, List<String> imgList,
                           List<String> tags, List<String> languages,
                           String g) {

}