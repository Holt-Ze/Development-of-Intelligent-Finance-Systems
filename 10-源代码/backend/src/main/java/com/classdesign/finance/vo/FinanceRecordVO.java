package com.classdesign.finance.vo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinanceRecordVO(
        Long id,
        String recordNo,
        Long deptId,
        String deptName,
        Long accountId,
        String accountName,
        Long categoryId,
        String categoryName,
        BigDecimal amount,
        LocalDate occurredOn,
        String operatorName,
        String remark,
        Integer status
) {
}
