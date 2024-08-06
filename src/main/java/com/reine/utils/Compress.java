package com.reine.utils;

import com.reine.properties.Profile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import net.lingala.zip4j.progress.ProgressMonitor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * 压缩工具类
 *
 * @author Iammm
 * 2024/8/5 10:23
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class Compress {
    private final Profile profile;

    /**
     * @param fileInput   源文件夹
     * @param allFileList 所有文件列表
     */
    private void getAllFiles(File fileInput, List<File> allFileList) {
        // 获取文件列表
        var startPath = Paths.get(fileInput.getPath());
        try {
            Files.walkFileTree(startPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    allFileList.add(file.toFile());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 对一个目录进行压缩
     *
     * @param zipName          压缩包名称
     * @param sourceFolder     源目录
     * @param passWord         压缩包密码
     * @param encryptionMethod 加密方式
     * @param aesKeyStrength   aes 密钥强度
     * @param splitSize        分片大小
     * @param compLevel        压缩等级
     * @return 任务是否成功
     * @throws IOException io 异常，由调用者处理
     */
    public boolean packageToZip(String zipName,
                                Path sourceFolder,
                                String passWord,
                                EncryptionMethod encryptionMethod,
                                AesKeyStrength aesKeyStrength,
                                int splitSize,
                                CompressionLevel compLevel) throws IOException {
        log.info("开始压缩");
        log.debug("源文件夹: {}, 密码: {}, 加密方法: {}, AES 的强度: {}",
                sourceFolder.toFile().getAbsolutePath(), passWord, encryptionMethod.name(), aesKeyStrength.name());
        var zipParameters = new ZipParameters();
        zipParameters.setEncryptFiles(encryptionMethod != EncryptionMethod.NONE);
        zipParameters.setEncryptionMethod(encryptionMethod);
        zipParameters.setAesKeyStrength(aesKeyStrength);
        zipParameters.setDefaultFolderPath(profile.getRootDir());
        zipParameters.setCompressionLevel(compLevel);
        zipParameters.setCompressionMethod(CompressionMethod.DEFLATE);
        var passWordChar = zh2AsciiArray(passWord);
        var zipFile = new ZipFile(zipName, passWordChar);
        zipFile.setRunInThread(true);
        List<File> allFileList = new ArrayList<>();
        getAllFiles(sourceFolder.toFile(), allFileList);
        if (splitSize != 0) zipFile.createSplitZipFile(allFileList, zipParameters, true, splitSize * 1048576L);
        else zipFile.addFiles(allFileList, zipParameters);
        var progressMonitor = zipFile.getProgressMonitor();
        while (progressMonitor.getState() == ProgressMonitor.State.BUSY) {
            printProgressBar(progressMonitor.getPercentDone());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        log.debug("压缩完成，路径: {}, 大小: {}KB, 分片数量: {}",
                zipFile.getFile().getAbsolutePath(),
                (zipFile.getFile().length() + (1024 * 1024) * zipFile.getSplitZipFiles().size() - 1),
                zipFile.getSplitZipFiles().size());
        log.info("压缩完成");
        log.warn("如果无法解压，请尝试使用 WinRAR (目前 7-zip 无法使用非 Ascii 字符密码解压，WinRAR 正常)");
        zipFile.close();
        return true;
    }

    /**
     * 中文或其它非 Ascii 字符到 Ascii char 数组
     *
     * @param zh 中文或其它非 Ascii 字符串
     * @return ascii char[]
     */
    private char[] zh2AsciiArray(String zh) {
        var ascii = new char[zh.length()];
        for (int i = 0; i < zh.length(); i++) {
            ascii[i] = zh.charAt(i);
        }
        return ascii;
    }


    /**
     * 对当前下载结果进行压缩
     *
     * @param name 压缩包名称
     */
    public boolean packageToZip(String name) throws IOException {
        var em = EncryptionMethod.NONE;
        var aesKeyStrength = AesKeyStrength.KEY_STRENGTH_128;
        if (!profile.getPassword().isEmpty()) {
            em = switch (profile.getEncryptionMethod()) {
                case 1 -> EncryptionMethod.ZIP_STANDARD;
                case 2 -> EncryptionMethod.ZIP_STANDARD_VARIANT_STRONG;
                case 3 -> EncryptionMethod.AES;
                case 4 -> {
                    aesKeyStrength = AesKeyStrength.KEY_STRENGTH_192;
                    yield EncryptionMethod.AES;
                }
                case 5 -> {
                    aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256;
                    yield EncryptionMethod.AES;
                }
                default -> throw new IllegalStateException("Unexpected value: " + profile.getEncryptionMethod());
            };
        }
        Files.createDirectories(Paths.get("", profile.getCompressDir()));

        return packageToZip(Paths.get(profile.getCompressDir(), name + ".zip").toString(),
                Paths.get(name, profile.getRootDir()),
                profile.getPassword(), em, aesKeyStrength,
                profile.getCompressSplitSize(), getCompLevel());
    }

    /**
     * 从配置文件解析压缩等级
     *
     * @return 解析后的压缩等级
     */
    private CompressionLevel getCompLevel() {
        return switch (profile.getCompressionLevel()) {
            case 1 -> CompressionLevel.NO_COMPRESSION;
            case 2 -> CompressionLevel.FASTEST;
            case 3 -> CompressionLevel.FAST;
            case 4 -> CompressionLevel.MEDIUM_FAST;
            // case 5 -> CompressionLevel.NORMAL;
            case 6 -> CompressionLevel.HIGHER;
            case 7 -> CompressionLevel.MAXIMUM;
            case 8 -> CompressionLevel.PRE_ULTRA;
            case 9 -> CompressionLevel.ULTRA;
            default -> CompressionLevel.NORMAL;
        };
    }

    /**
     * 打印进度条
     *
     * @param currentProgress 当前进度
     */
    private void printProgressBar(int currentProgress) {
        var progressBarLength = 50; // 进度条长度
        var progress = (int) ((currentProgress / (double) 100) * progressBarLength);
        var progressBar = "\r压缩进度：[" +
                "#".repeat(progress) +
                " ".repeat(progressBarLength - progress) +
                "] " + currentProgress + "%" + "\r";
       if (profile.getCompressProgressBarVisible()) {
           if (profile.getCompressProgressBarImpl() == 2)
               System.out.print(progressBar);
           else log.info(progressBar);
       }
    }

}

