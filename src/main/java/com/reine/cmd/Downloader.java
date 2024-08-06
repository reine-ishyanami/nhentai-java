package com.reine.cmd;

import com.reine.properties.Profile;
import com.reine.site.SiteAction;
import com.reine.utils.PdfUtils;
import java.io.IOException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;


/**
 * @author reine 2024/8/4 9:20
 */
@ShellComponent
@RequiredArgsConstructor
@Slf4j
public class Downloader {

  private final SiteAction action;

  private final Profile profile;

  private final PdfUtils pdfUtils;

  /**
   * 下载hentai
   *
   * @param name hentai名称
   */
  @ShellMethod(key = "download", value = "download hentai")
  public void download(@ShellOption(help = "hentai name") String name,
      @ShellOption(help = "compress", defaultValue = "false") Boolean compress,
      @ShellOption(help = "convert to pdf", defaultValue = "false") Boolean convert,
      @ShellOption(help = "overwrite pdf if exists", defaultValue = "false") Boolean overwrite) {
    action.search(name);
    actionDownload(compress, convert, overwrite);
  }

  /**
   * 随机下载 hentai
   */
  @ShellMethod(key = "random", value = "download hentai randomly")
  public void random(@ShellOption(help = "compress", defaultValue = "false") Boolean compress,
      @ShellOption(help = "convert to pdf", defaultValue = "false") Boolean convert,
      @ShellOption(help = "overwrite pdf if exists", defaultValue = "false") Boolean overwrite) {
    action.random();
    actionDownload(compress, convert, overwrite);
  }

  private void actionDownload(Boolean compress, Boolean convert, Boolean overwrite) {
    action.download()
        .forEach(fail -> log.atLevel(fail.logLevel())
            .log("{} 文件下载失败，原因: {}", fail.fileName(), fail.reason()));
    if (profile.getCompress().getEnable() || compress) {
      try {
        action.compress();
      } catch (IOException e) {
        log.error("打包失败");
      }
    }
    if (profile.getPdf().getEnable() || convert) {
      try {
        action.convertToPdf(overwrite);
      } catch (IOException e) {
        log.error("转换失败");
      }
    }
  }

  /**
   * 将一组图片转换成 pdf
   *
   * @param path 图片路径
   * @param name pdf 名称
   */
  @ShellMethod(key = "convert", value = "convert hentai to pdf")
  public void convert(@ShellOption(help = "hentai images path") String path,
      @ShellOption(help = "pdf name") String name,
      @ShellOption(help = "overwrite pdf if exists", defaultValue = "false") Boolean overwrite) {
    log.info("images path: {}", path);
    try {
      pdfUtils.convertToPdf(Path.of(path), Path.of(profile.getPdf().getDir(), name + ".pdf"),
          overwrite);
    } catch (IOException e) {
      log.error("转换失败");
    }
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
    profile.getCompress().setPassword(password);
    profile.getCompress().setEnable(true);
    return "Update Password Success";
  }
}
