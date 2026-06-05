package com.classdesign.finance.vo;

public record BackupFileVO(Long id, String backupName, String filePath, Long fileSize, String createdBy,
                           String createdAt, String restoredAt, String remark) {
}
