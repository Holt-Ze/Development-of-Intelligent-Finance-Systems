package com.classdesign.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountSaveRequest(
        @NotBlank(message = "账户编号不能为空") String accountCode,
        @NotBlank(message = "账户名称不能为空") String accountName,
        String ownerUnit,
        String description,
        @NotNull(message = "状态不能为空") Integer status
) {
}
