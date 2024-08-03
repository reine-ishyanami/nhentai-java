package com.reine;


import com.reine.config.Profile;
import com.reine.site.impl.NHentaiSiteAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;

/**
 * @author reine
 * 2024/7/17 23:18
 */

@Command(name = "Hentai Downloader", version = "Hentai Downloader 1.0", mixinStandardHelpOptions = true)
public class Downloader implements Runnable {

    @Option(names = {"-n", "--name"},
            echo = true,
            description = "Hentai name",
            prompt = "Please enter hentai name ",
            interactive = true)
    String hentaiName;

    private static final Logger log = LoggerFactory.getLogger(Downloader.class);
    private static final Profile profile = Profile.getProfile();

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[]{"-n"};
        }
        int exitCode = new CommandLine(new Downloader()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        var hentai = hentaiName;
        NHentaiSiteAction action = NHentaiSiteAction.getInstance();
        action.search(hentai);
        action.download().forEach(fail -> log.error("{} 下载失败, 原因 {}", fail.fileName(), fail.reason()));
        if (profile.getCompress()) {
            try {
                action.packageTo7z();
            } catch (IOException e) {
                log.error("打包失败");
            }
        }
    }
}