package com.classdesign.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("backup_record")
public class BackupRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String backupName;
    private String filePath;
    private Long fileSize;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime restoredAt;
    private String remark;
}
