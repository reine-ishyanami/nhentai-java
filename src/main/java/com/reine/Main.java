package com.reine;


import com.reine.config.Profile;
import com.reine.site.impl.NHentaiSiteAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Scanner;

/**
 * @author reine
 * 2024/7/17 23:18
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final Profile profile = Profile.getProfile();

    public static void main(String[] args) throws IOException {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("请输入要下载的hentai名称: ");
            var hentai = scanner.nextLine();
            NHentaiSiteAction action = NHentaiSiteAction.getInstance();
            action.search(hentai);
            action.download().forEach(fail -> log.error("{} 下载失败, 原因 {}", fail.fileName(), fail.reason()));
            if (profile.getCompress()) {
                action.packageTo7z();
            }
            System.exit(0);
        }
    }
}