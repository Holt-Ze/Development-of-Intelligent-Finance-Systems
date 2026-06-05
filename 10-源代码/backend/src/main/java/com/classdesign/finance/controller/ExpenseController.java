package com.classdesign.finance.controller;

import com.classdesign.finance.common.ApiResponse;
import com.classdesign.finance.dto.FinanceRecordSaveRequest;
import com.classdesign.finance.dto.StatusUpdateRequest;
import com.classdesign.finance.service.RecordService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final RecordService recordService;

    public ExpenseController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam(required = false) Long deptId,
                               @RequestParam(required = false) Long accountId,
                               @RequestParam(required = false) Long categoryId,
                               @RequestParam(required = false) String startDate,
                               @RequestParam(required = false) String endDate) {
        return ApiResponse.success(recordService.listExpenses(deptId, accountId, categoryId, startDate, endDate));
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody FinanceRecordSaveRequest request) {
        return ApiResponse.success(recordService.createExpense(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody FinanceRecordSaveRequest request) {
        return ApiResponse.success(recordService.updateExpense(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<?> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        recordService.updateExpenseStatus(id, request.status());
        return ApiResponse.success(Map.of("message", "支出记录状态更新成功", "id", id, "status", request.status()));
    }
}
