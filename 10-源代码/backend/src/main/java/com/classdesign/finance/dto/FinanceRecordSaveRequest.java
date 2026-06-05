package com.classdesign.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinanceRecordSaveRequest(
        @NotNull(message = "部门不能为空") Long deptId,
        @NotNull(message = "账户不能为空") Long accountId,
        @NotNull(message = "类别不能为空") Long categoryId,
        @NotNull(message = "金额不能为空")
        @DecimalMin(value = "0.01", message = "金额必须大于 0")
        BigDecimal amount,
        @NotNull(message = "发生日期不能为空") LocalDate occurredOn,
        @NotBlank(message = "经办人不能为空") String operatorName,
        String remark,
        @NotNull(message = "状态不能为空") Integer status
) {
}
