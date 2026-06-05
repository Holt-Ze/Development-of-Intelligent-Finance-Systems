package com.classdesign.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("account_info")
public class AccountInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String accountCode;
    private String accountName;
    private String ownerUnit;
    private String description;
    private Integer status;
    private LocalDateTime createdAt;
}
