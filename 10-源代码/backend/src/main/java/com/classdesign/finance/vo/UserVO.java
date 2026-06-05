package com.classdesign.finance.vo;

public record UserVO(
        Long id,
        String username,
        String realName,
        String phone,
        String roleCode,
        String roleName,
        Integer status
) {
}
