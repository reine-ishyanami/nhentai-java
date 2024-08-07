package com.reine.utils;

import java.io.File;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author reine
 * 2024/8/5 18:23
 */
@Getter
@Component
@Slf4j
public class PdfUtils {

    public File currentPdf;

    /**
     * 将指定文件夹下的所有图片，转换成一个 pdf 文件
     *
     * @param filePath  图片路径
     * @param pdfPath   生成 pdf 的路径
     * @param overwrite 是否覆盖原有文件
     * @return 是否成功
     */
    public boolean convertToPdf(Path filePath, Path pdfPath, boolean overwrite) throws IOException {
        // 创建 pdf 文件
        if (pdfPath.toFile().exists() && !overwrite) {
            log.warn("pdf 文件已存在，不进行操作");
            return false;
        }
        log.info("开始将图片文件夹转换成 pdf 文件");
        // 创建一个新的PDDocument对象
        try (
                var document = new PDDocument();
                var files = Files.newDirectoryStream(filePath)
        ) {
            List<Path> paths = getSortFiles(files);
            // 获取图片文件夹中的所有图片文件
            for (var path : paths) {
                // 创建一个新的页面
                var page = new PDPage();
                document.addPage(page);
                // 加载图片
                var pdImage = PDImageXObject.createFromFileByExtension(path.toFile(), document);
                // 将图片添加到页面
                var contentStream = new PDPageContentStream(document, page);
                contentStream.drawImage(pdImage, 0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
                log.debug("添加图片 {} 到 pdf 文件", path.toFile().getName());
                contentStream.close();
            }
            document.save(pdfPath.toString());
            currentPdf = pdfPath.toFile();
            log.info("转换完成");
        }
        return true;
    }

    /**
     * 排序文件
     *
     * @param files 原文件序列
     * @return 排序后序列
     */
    private List<Path> getSortFiles(DirectoryStream<Path> files) {
        List<Path> paths = new ArrayList<>();
        for (Path file : files) {
            paths.add(file);
        }
        paths.sort((o1, o2) -> {
            Integer o1Num = Integer.parseInt(o1.toFile().getName().split("\\.")[0]);
            Integer o2Num = Integer.parseInt(o2.toFile().getName().split("\\.")[0]);
            return o1Num.compareTo(o2Num); // 升序排序
        });
        return paths;
    }
}
