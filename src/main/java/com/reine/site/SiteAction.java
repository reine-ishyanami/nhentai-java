package com.reine.site;

import com.reine.entity.FailResult;
import com.reine.entity.HentaiDetail;

import java.io.IOException;
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
    boolean packageTo7z() throws IOException;

}
