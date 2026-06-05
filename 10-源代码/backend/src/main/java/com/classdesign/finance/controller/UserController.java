package com.classdesign.finance.controller;

import com.classdesign.finance.common.ApiResponse;
import com.classdesign.finance.dto.StatusUpdateRequest;
import com.classdesign.finance.dto.UserSaveRequest;
import com.classdesign.finance.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.success(userService.list());
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody UserSaveRequest request) {
        return ApiResponse.success(userService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody UserSaveRequest request) {
        return ApiResponse.success(userService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<?> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        userService.updateStatus(id, request.status());
        return ApiResponse.success(Map.of("message", "用户状态更新成功", "id", id, "status", request.status()));
    }
}
