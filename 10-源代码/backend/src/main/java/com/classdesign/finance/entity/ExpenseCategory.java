package com.classdesign.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("expense_category")
public class ExpenseCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String categoryCode;
    private String categoryName;
    private Integer status;
    private LocalDateTime createdAt;
}
