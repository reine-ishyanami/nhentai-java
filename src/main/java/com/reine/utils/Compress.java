package com.reine.utils;

import com.reine.properties.Profile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
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

    private void getAllFiles(File fileInput, List<File> allFileList) {
        // 获取文件列表
        Path startPath = Paths.get(fileInput.getPath());
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

    public boolean packageToZip(String zipName, Path sourceFolder, String passWord, EncryptionMethod encryptionMethod, AesKeyStrength aesKeyStrength, int splitSize) throws IOException {
        log.info("开始压缩");
        log.info("源文件夹: {} , 密码: {} , 加密方法: {}, AES 的强度: {}", sourceFolder.toFile().getAbsolutePath(), passWord, encryptionMethod.name(), aesKeyStrength.name());
        var zip = new ZipParameters();
        zip.setEncryptFiles(encryptionMethod != EncryptionMethod.NONE);
        zip.setEncryptionMethod(encryptionMethod);
        zip.setAesKeyStrength(aesKeyStrength);
        zip.setDefaultFolderPath(profile.getRootDir());
        var zipFile = new ZipFile(zipName, passWord.toCharArray());
        List<File> allFileList = new ArrayList<>();
        getAllFiles(sourceFolder.toFile(), allFileList);
        if (splitSize != 0)
            zipFile.createSplitZipFile(allFileList, zip, true, splitSize * 1048576L);
        else zipFile.addFiles(allFileList, zip);
        log.info("压缩完成, 路径: {}, 大小: {}, 分片数量: {}", zipFile.getFile().getAbsolutePath(), (zipFile.getFile().length() + (1024 * 1024) * zipFile.getSplitZipFiles().size() - 1) + "KB", zipFile.getSplitZipFiles().size());
        zipFile.close();
        return true;
    }

    public boolean packageToZip(String name) throws IOException {
        var em = EncryptionMethod.NONE;
        AesKeyStrength aesKeyStrength = AesKeyStrength.KEY_STRENGTH_128;
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
        return packageToZip(Paths.get(profile.getCompressDir(), name + ".zip").toString(), Paths.get(name, profile.getRootDir()), profile.getPassword(), em, aesKeyStrength, profile.getCompressSplitSize());
    }
}

