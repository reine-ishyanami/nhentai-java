package com.reine.controller;


import com.reine.entity.Preview;
import com.reine.site.SiteAction;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * http接口，根据文件名和标签下载/预览hentai
 *
 * @author Iammm 2024/8/7 10:28
 */
@Controller
@Slf4j
@RequiredArgsConstructor
@RestController
public class DownloadController {

  private final SiteAction action;

  /**
   * 预览 hentai
   *
   * @param name  名字
   * @param label 标签
   * @return 预览
   */
  @GetMapping("/preview")
  public ResponseEntity<Preview> download(@RequestParam String name,
      @RequestParam List<String> label) {
    if (name.isEmpty() && label.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } else {
      Preview preview;
      try {
        if (name.isEmpty()) {
          preview = randomByLabelRange(label);
        } else {
          preview = searchByName(name);
        }
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }

      return new ResponseEntity<>(preview, HttpStatus.OK);
    }
  }

  /**
   * 下载 hentai 通过 id
   *
   * @param fileType 1：pdf 2：zip
   * @param password 密码，留空不加密，aes 256加密算法，pdf 暂不支持加密
   * @param id       hentai 的 id
   * @return 文件
   */
  @GetMapping("/")
  public ResponseEntity<FileSystemResource> download(@RequestParam Integer fileType,
      @RequestParam String password, @RequestParam String id) {
    FileSystemResource fileResource;
    HttpHeaders headers = new HttpHeaders();
    if (id.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    switch (fileType) {
      case 1:
        try {
          fileResource = new FileSystemResource(action.getPdf(id, password));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", id + ".pdf");
        break;
      case 2:
        try {
          fileResource = new FileSystemResource(action.getZip(id, password));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", id + ".zip");
        break;
      default:
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(fileResource, headers, HttpStatus.OK);
  }

  private Preview randomByLabelRange(List<String> labels) throws MalformedURLException {
    try {
      action.search(list2String(labels), true);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return new Preview(labels.get(0), action.getLabels(), action.getGalleryId(), action.getCover(),
        action.getLang());
  }

  private String list2String(List<String> list) {
    StringBuilder rs = new StringBuilder(" ");
    for (String s : list) {
      rs.append("+").append(s);
    }
    rs.append("+");
    return rs.toString();
  }

  private Preview searchByName(String name) throws MalformedURLException {
    action.search(name, false);
    return new Preview(name, action.getLabels(), action.getGalleryId(), action.getCover(),
        action.getLang());
  }

}
