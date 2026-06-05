package com.classdesign.finance.controller;

import com.classdesign.finance.common.ApiResponse;
import com.classdesign.finance.dto.AccountSaveRequest;
import com.classdesign.finance.dto.StatusUpdateRequest;
import com.classdesign.finance.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.success(accountService.list());
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody AccountSaveRequest request) {
        return ApiResponse.success(accountService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody AccountSaveRequest request) {
        return ApiResponse.success(accountService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<?> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        accountService.updateStatus(id, request.status());
        return ApiResponse.success(Map.of("message", "账户状态更新成功", "id", id, "status", request.status()));
    }
}
