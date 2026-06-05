package com.classdesign.finance.controller;

import com.classdesign.finance.common.ApiResponse;
import com.classdesign.finance.dto.CategorySaveRequest;
import com.classdesign.finance.dto.StatusUpdateRequest;
import com.classdesign.finance.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/income-categories")
public class IncomeCategoryController {

    private final CategoryService categoryService;

    public IncomeCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.success(categoryService.listIncomeCategories());
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody CategorySaveRequest request) {
        return ApiResponse.success(categoryService.createIncomeCategory(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody CategorySaveRequest request) {
        return ApiResponse.success(categoryService.updateIncomeCategory(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<?> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        categoryService.updateIncomeCategoryStatus(id, request.status());
        return ApiResponse.success(Map.of("message", "收入类别状态更新成功", "id", id, "status", request.status()));
    }
}
