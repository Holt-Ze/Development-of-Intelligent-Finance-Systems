package com.classdesign.finance.vo;

import java.util.List;
import java.util.Map;

public record ReportSummaryVO(
        Map<String, Object> query,
        Double totalIncome,
        Double totalExpense,
        Double balance,
        List<StatItemVO> categoryStats,
        List<StatItemVO> departmentStats,
        List<TrendPointVO> trendStats
) {
}
