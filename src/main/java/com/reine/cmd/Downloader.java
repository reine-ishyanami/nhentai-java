package com.reine.cmd;

import com.reine.properties.Profile;
import com.reine.site.SiteAction;
import com.reine.site.impl.NHentaiSiteAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;


/**
 * @author reine
 * 2024/8/4 9:20
 */
@ShellComponent
@RequiredArgsConstructor
@Slf4j
public class Downloader {

    private final SiteAction action;

    private final Profile profile;

    @ShellMethod(key = "download", value = "download hentai")
    public void download(@ShellOption(help = "hentai name") String name) {
        action.search(name);
        action.download().forEach(fail -> log.error("{} 下载失败, 原因 {}", fail.fileName(), fail.reason()));
        if (profile.getCompress()) {
            try {
                action.packageTo7z();
            } catch (IOException e) {
                log.error("打包失败");
            }
        }
    }

    @ShellMethod(key = "exit", value = "exit")
    public void exit(@Autowired ConfigurableApplicationContext context) {
        context.close();
    }
}
