package com.reine.site;

import com.reine.annotation.Timer;
import com.reine.entity.FailResult;
import com.reine.entity.HentaiDetail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

/**
 * @author reine
 * 2024/7/20 11:03
 */
public interface SiteAction {

    /**
     * 获取网站地址
     *
     * @return 网站地址
     */
    String baseUrl();

    @Timer
    HentaiDetail search(String name, Boolean isRandom);

    /**
     * 搜索本子详细信息
     *
     * @param name 本子名称
     * @return 本子详细信息
     */
    HentaiDetail search(String name);

    /**
     * 开启下载任务
     *
     * @return 下载失败文件及其失败原因
     */
    List<FailResult> download();

    /**
     * 将文件夹打包成7z
     *
     * @return 成功与否
     */
    boolean compress() throws IOException;

    /**
     * 将文件夹中的所有图片合并成单个 pdf 文件
     *
     * @param overwrite 是否覆盖
     * @return 成功与否
     */
    boolean convertToPdf(Boolean overwrite) throws IOException;

    /**
     * 获取所有标签
     *
     * @return 标签列表
     */
    List<String> getLabels();

    /**
     * 获取封面
     *
     * @return 封面 url
     */
    String getCover() throws MalformedURLException;

    /**
     * 获取唯一的画廊id
     *
     * @return 画廊id
     */
    String getGalleryId();

    /**
     * 获取语言
     *
     * @return 语言
     */
    List<String> getLang();

    /**
     *  获取 hentai zip 文件
     * @param id 画廊 id
     * @param password 密码
     * @return zip 文件对象
     * @throws IOException io 异常
     */
    File getZip(String id, String password) throws IOException;

    /**
     *  获取 hentai pdf 文件
     * @param id 画廊 id
     * @param password 密码//TODO 实现 pdf 的加密
     * @return pdf 文件对象
     * @throws IOException io 异常
     */
    File getPdf(String id, String password) throws IOException;
}
