package com.classdesign.finance.controller;

import com.classdesign.finance.common.ApiResponse;
import com.classdesign.finance.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/weekly")
    public ApiResponse<?> weekly(@RequestParam(required = false) String week,
                                 @RequestParam(required = false) Long deptId) {
        return ApiResponse.success(reportService.weekly(week, deptId));
    }

    @GetMapping("/monthly")
    public ApiResponse<?> monthly(@RequestParam(required = false) String month,
                                  @RequestParam(required = false) Long deptId) {
        return ApiResponse.success(reportService.monthly(month, deptId));
    }

    @GetMapping("/yearly")
    public ApiResponse<?> yearly(@RequestParam(required = false) String year,
                                 @RequestParam(required = false) Long deptId) {
        return ApiResponse.success(reportService.yearly(year, deptId));
    }

    @GetMapping("/custom")
    public ApiResponse<?> custom(@RequestParam(required = false) String startDate,
                                 @RequestParam(required = false) String endDate,
                                 @RequestParam(required = false) Long deptId) {
        return ApiResponse.success(reportService.custom(startDate, endDate, deptId));
    }

    @GetMapping("/dashboard")
    public ApiResponse<?> dashboard() {
        return ApiResponse.success(reportService.dashboard());
    }
}
