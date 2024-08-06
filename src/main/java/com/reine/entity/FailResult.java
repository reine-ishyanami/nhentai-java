package com.reine.entity;

import org.slf4j.event.Level;

/**
 * 下载失败文件名称及其下载失败原因
 *
 * @param fileName 文件名称
 * @param reason   下载失败原因
 * @author reine
 * 2024/7/18 15:54
 */
public record FailResult(String fileName, String reason, Level logLevel) {
}
