package com.classdesign.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategorySaveRequest(
        @NotBlank(message = "类别编号不能为空") String categoryCode,
        @NotBlank(message = "类别名称不能为空") String categoryName,
        @NotNull(message = "状态不能为空") Integer status
) {
}
