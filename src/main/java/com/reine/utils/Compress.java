package com.reine.utils;

import com.reine.properties.Profile;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
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
public class Compress {
    private static final Logger log = LoggerFactory.getLogger(Compress.class);
    private final Profile profile;

    private static void getAllFiles(File fileInput, List<File> allFileList) {
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

    private boolean packageToZip(String zipName, URI sourceFolder, String passWord, EncryptionMethod encryptionMethod, AesKeyStrength aesKeyStrength, int splitSize) {
        log.info("开始压缩");
        log.info("source文件夹: {} , password:{} , 加密方法：{},AesKeyStrength:{}", sourceFolder, passWord, encryptionMethod.name(), aesKeyStrength.name());

        var zip = new ZipParameters();
        zip.setEncryptFiles(encryptionMethod != EncryptionMethod.NONE);
        zip.setEncryptionMethod(encryptionMethod);

        zip.setAesKeyStrength(aesKeyStrength);

        zip.setDefaultFolderPath(profile.getRootDir());
        try (var zipFile = new ZipFile(zipName, passWord.toCharArray())) {
            List<File> allFileList = new ArrayList<>();
            getAllFiles(new File(sourceFolder), allFileList);

            if (splitSize != 0)
                zipFile.createSplitZipFile(allFileList, zip, true, splitSize * 1048576L);
            else zipFile.addFiles(allFileList, zip);
            log.info("压缩完成,path:{},size:{},分片数量:{}", zipFile.getFile().getAbsolutePath(), (zipFile.getFile().length() + (1024 * 1024) * zipFile.getSplitZipFiles().size() - 1) + "KB", zipFile.getSplitZipFiles().size());
        } catch (Exception IOException) {
            log.error("压缩失败", IOException);
        }
        // throw new UnsupportedOperationException();
        return true;
    }

    public boolean packageToZip(String name) {
        var em = EncryptionMethod.NONE;
        AesKeyStrength aesKeyStrength = AesKeyStrength.KEY_STRENGTH_128;
        if (!profile.getPassword().isEmpty()) {
            switch (profile.getEncryptionMethod()) {
                case 0:
                    break;
                case 1:
                    em = EncryptionMethod.ZIP_STANDARD;
                    break;
                case 2:
                    em = EncryptionMethod.ZIP_STANDARD_VARIANT_STRONG;
                    break;
                case 3:
                    em = EncryptionMethod.AES;
                    break;
                case 4:
                    em = EncryptionMethod.AES;
                    aesKeyStrength = AesKeyStrength.KEY_STRENGTH_192;
                    break;
                case 5:
                    em = EncryptionMethod.AES;
                    aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256;
                    break;
            }
        }
        if (!new File(getFolder("", profile.getCompressDir()).getPath()).exists()) {
            try {
                Files.createDirectories(Path.of(getFolder("", profile.getCompressDir())));
            } catch (IOException e) {
                log.error("Error creating directory", e);
                throw new RuntimeException(e);
            }
        }
        return packageToZip(getFolder(name + ".zip", profile.getCompressDir()).getPath(), getFolder(name, profile.getRootDir()), profile.getPassword(), em, aesKeyStrength, profile.getCompressSplitSize());
    }

    private URI getFolder(String name, String dir) {
        return
                Paths.get(System.getProperty("user.dir"), dir, name).toUri();


    }
}

