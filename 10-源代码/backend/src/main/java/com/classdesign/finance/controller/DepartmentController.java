package com.classdesign.finance.controller;

import com.classdesign.finance.common.ApiResponse;
import com.classdesign.finance.dto.DepartmentSaveRequest;
import com.classdesign.finance.dto.StatusUpdateRequest;
import com.classdesign.finance.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.success(departmentService.list());
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody DepartmentSaveRequest request) {
        return ApiResponse.success(departmentService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody DepartmentSaveRequest request) {
        return ApiResponse.success(departmentService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<?> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        departmentService.updateStatus(id, request.status());
        return ApiResponse.success(Map.of("message", "部门状态更新成功", "id", id, "status", request.status()));
    }
}
