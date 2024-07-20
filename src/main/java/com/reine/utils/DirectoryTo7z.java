package com.reine.utils;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 将目录压缩成7z压缩包
 *
 * @author reine
 */
public class DirectoryTo7z {

    /**
     * 压缩文件夹为7z格式，并附加密码
     *
     * @param sourceDir 被压缩文件夹路径
     * @param output7z  输出文件路径
     * @param password  TODO 添加密码
     * @throws IOException
     */
    public static void compressDirectoryTo7z(File sourceDir, File output7z, String password) throws IOException {
        try (SevenZOutputFile sevenZOutput = new SevenZOutputFile(output7z)) {
            addFilesToArchive(sevenZOutput, sourceDir, "");
        }
    }

    private static void addFilesToArchive(SevenZOutputFile sevenZOutput, File file, String base) throws IOException {
        String entryName = base + file.getName();
        if (file.isDirectory()) {
            entryName += "/";
            SevenZArchiveEntry entry = sevenZOutput.createArchiveEntry(file, entryName);
            sevenZOutput.putArchiveEntry(entry);
            sevenZOutput.closeArchiveEntry();
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    addFilesToArchive(sevenZOutput, child, entryName);
                }
            }
        } else {
            SevenZArchiveEntry entry = sevenZOutput.createArchiveEntry(file, entryName);
            sevenZOutput.putArchiveEntry(entry);
            try (FileInputStream fis = new FileInputStream(file)) {
                sevenZOutput.write(fis);
            }
            sevenZOutput.closeArchiveEntry();
        }
    }
}
