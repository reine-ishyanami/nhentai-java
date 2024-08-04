package com.reine.cmd;

import com.reine.properties.Profile;
import com.reine.site.SiteAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;


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

    /**
     * 下载hentai
     *
     * @param name hentai名称
     */
    @ShellMethod(key = "download", value = "download hentai")
    public void download(@ShellOption(help = "hentai name") String name) {
        action.search(name);
        action.download().forEach(fail -> log.error("{} 下载失败, 原因 {}", fail.fileName(), fail.reason()));
        if (profile.getCompress()) {
            try {
                action.packageTo7z(name, getFolder(name), profile.getPassword(), EncryptionMethod.AES, AesKeyStrength.KEY_STRENGTH_256);
            } catch (IOException e) {
                log.error("打包失败:{}", e.getMessage());
            }
        }
    }

    private URI getFolder(String name) {
        Path paths =
                Paths.get(System.getProperty("user.dir"),profile.getRootDir(),name);

        return paths.toUri();

    }

    /**
     * 设置下载路径
     *
     * @param dir 下载路径
     * @return 更新成功
     */
    @ShellMethod(key = "cd", value = "change directory of download")
    public String setRootDir(@ShellOption(help = "download directory") String dir) {
        profile.setRootDir(dir);
        return "Update Directory Success";
    }

    /**
     * 设置压缩密码
     *
     * @param password 压缩密码
     * @return 更新成功
     */
    @ShellMethod(key = "password", value = "package password")
    public String setPassword(@ShellOption(help = "password") String password) {
        profile.setPassword(password);
        return "Update Password Success";
    }
}
