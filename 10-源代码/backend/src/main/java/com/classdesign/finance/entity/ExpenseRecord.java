package com.classdesign.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("expense_record")
public class ExpenseRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String recordNo;
    private Long deptId;
    private Long accountId;
    private Long categoryId;
    private BigDecimal amount;
    private LocalDate occurredOn;
    private String operatorName;
    private String remark;
    private Integer status;
    private LocalDateTime createdAt;
}
