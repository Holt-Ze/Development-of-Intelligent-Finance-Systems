package com.classdesign.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserSaveRequest(
        @NotBlank(message = "用户名不能为空") String username,
        String password,
        @NotBlank(message = "姓名不能为空") String realName,
        String phone,
        @NotBlank(message = "角色不能为空") String roleCode,
        @NotNull(message = "状态不能为空") Integer status
) {
}
