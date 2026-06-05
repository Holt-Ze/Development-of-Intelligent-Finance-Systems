package com.classdesign.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DepartmentSaveRequest(
        @NotBlank(message = "部门编号不能为空") String deptCode,
        @NotBlank(message = "部门名称不能为空") String deptName,
        String leaderName,
        String phone,
        @NotNull(message = "状态不能为空") Integer status
) {
}
