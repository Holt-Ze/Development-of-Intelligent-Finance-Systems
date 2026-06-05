package com.classdesign.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classdesign.finance.common.BusinessException;
import com.classdesign.finance.dto.FinanceRecordSaveRequest;
import com.classdesign.finance.entity.AccountInfo;
import com.classdesign.finance.entity.DeptInfo;
import com.classdesign.finance.entity.ExpenseCategory;
import com.classdesign.finance.entity.ExpenseRecord;
import com.classdesign.finance.entity.IncomeCategory;
import com.classdesign.finance.entity.IncomeRecord;
import com.classdesign.finance.mapper.ExpenseRecordMapper;
import com.classdesign.finance.mapper.IncomeRecordMapper;
import com.classdesign.finance.vo.FinanceRecordVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecordService {

    private static final DateTimeFormatter RECORD_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final IncomeRecordMapper incomeRecordMapper;
    private final ExpenseRecordMapper expenseRecordMapper;
    private final DepartmentService departmentService;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final JdbcTemplate jdbcTemplate;

    public RecordService(IncomeRecordMapper incomeRecordMapper,
                         ExpenseRecordMapper expenseRecordMapper,
                         DepartmentService departmentService,
                         AccountService accountService,
                         CategoryService categoryService,
                         JdbcTemplate jdbcTemplate) {
        this.incomeRecordMapper = incomeRecordMapper;
        this.expenseRecordMapper = expenseRecordMapper;
        this.departmentService = departmentService;
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> listIncomes(Long deptId, Long accountId, Long categoryId, String startDate, String endDate) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("query", buildQuery(deptId, accountId, categoryId, startDate, endDate));
        result.put("records", queryRecords(true, deptId, accountId, categoryId, startDate, endDate));
        return result;
    }

    public Map<String, Object> listExpenses(Long deptId, Long accountId, Long categoryId, String startDate, String endDate) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("query", buildQuery(deptId, accountId, categoryId, startDate, endDate));
        result.put("records", queryRecords(false, deptId, accountId, categoryId, startDate, endDate));
        return result;
    }

    @Transactional
    public FinanceRecordVO createIncome(FinanceRecordSaveRequest request) {
        validateIncomeRequest(request);
        IncomeRecord record = new IncomeRecord();
        fillIncomeRecord(record, request);
        record.setRecordNo(generateRecordNo("IN", request.occurredOn(), true));
        record.setCreatedAt(LocalDateTime.now());
        incomeRecordMapper.insert(record);
        return getIncomeRecordVO(record.getId());
    }

    @Transactional
    public FinanceRecordVO updateIncome(Long id, FinanceRecordSaveRequest request) {
        validateIncomeRequest(request);
        IncomeRecord record = getIncomeRecord(id);
        fillIncomeRecord(record, request);
        incomeRecordMapper.updateById(record);
        return getIncomeRecordVO(id);
    }

    public void updateIncomeStatus(Long id, Integer status) {
        IncomeRecord record = getIncomeRecord(id);
        record.setStatus(status);
        incomeRecordMapper.updateById(record);
    }

    @Transactional
    public FinanceRecordVO createExpense(FinanceRecordSaveRequest request) {
        validateExpenseRequest(request);
        ExpenseRecord record = new ExpenseRecord();
        fillExpenseRecord(record, request);
        record.setRecordNo(generateRecordNo("EX", request.occurredOn(), false));
        record.setCreatedAt(LocalDateTime.now());
        expenseRecordMapper.insert(record);
        return getExpenseRecordVO(record.getId());
    }

    @Transactional
    public FinanceRecordVO updateExpense(Long id, FinanceRecordSaveRequest request) {
        validateExpenseRequest(request);
        ExpenseRecord record = getExpenseRecord(id);
        fillExpenseRecord(record, request);
        expenseRecordMapper.updateById(record);
        return getExpenseRecordVO(id);
    }

    public void updateExpenseStatus(Long id, Integer status) {
        ExpenseRecord record = getExpenseRecord(id);
        record.setStatus(status);
        expenseRecordMapper.updateById(record);
    }

    private Map<String, Object> buildQuery(Long deptId, Long accountId, Long categoryId, String startDate, String endDate) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("deptId", deptId);
        query.put("accountId", accountId);
        query.put("categoryId", categoryId);
        query.put("startDate", startDate);
        query.put("endDate", endDate);
        return query;
    }

    private List<FinanceRecordVO> queryRecords(boolean income, Long deptId, Long accountId, Long categoryId, String startDate, String endDate) {
        String tableName = income ? "income_record" : "expense_record";
        String categoryTable = income ? "income_category" : "expense_category";
        String sql = """
                SELECT r.id, r.record_no, r.dept_id, d.dept_name, r.account_id, a.account_name,
                       r.category_id, c.category_name, r.amount, r.occurred_on, r.operator_name, r.remark, r.status
                FROM %s r
                JOIN dept_info d ON r.dept_id = d.id
                JOIN account_info a ON r.account_id = a.id
                JOIN %s c ON r.category_id = c.id
                WHERE 1 = 1
                """.formatted(tableName, categoryTable);
        List<Object> params = new ArrayList<>();
        StringBuilder builder = new StringBuilder(sql);
        if (deptId != null) {
            builder.append(" AND r.dept_id = ? ");
            params.add(deptId);
        }
        if (accountId != null) {
            builder.append(" AND r.account_id = ? ");
            params.add(accountId);
        }
        if (categoryId != null) {
            builder.append(" AND r.category_id = ? ");
            params.add(categoryId);
        }
        if (startDate != null && !startDate.isBlank()) {
            builder.append(" AND r.occurred_on >= ? ");
            params.add(startDate);
        }
        if (endDate != null && !endDate.isBlank()) {
            builder.append(" AND r.occurred_on <= ? ");
            params.add(endDate);
        }
        builder.append(" ORDER BY r.occurred_on DESC, r.id DESC ");
        return jdbcTemplate.query(builder.toString(), (rs, rowNum) -> new FinanceRecordVO(
                rs.getLong("id"),
                rs.getString("record_no"),
                rs.getLong("dept_id"),
                rs.getString("dept_name"),
                rs.getLong("account_id"),
                rs.getString("account_name"),
                rs.getLong("category_id"),
                rs.getString("category_name"),
                rs.getBigDecimal("amount"),
                rs.getDate("occurred_on").toLocalDate(),
                rs.getString("operator_name"),
                rs.getString("remark"),
                rs.getInt("status")
        ), params.toArray());
    }

    private FinanceRecordVO getIncomeRecordVO(Long id) {
        List<FinanceRecordVO> records = jdbcTemplate.query("""
                        SELECT r.id, r.record_no, r.dept_id, d.dept_name, r.account_id, a.account_name,
                               r.category_id, c.category_name, r.amount, r.occurred_on, r.operator_name, r.remark, r.status
                        FROM income_record r
                        JOIN dept_info d ON r.dept_id = d.id
                        JOIN account_info a ON r.account_id = a.id
                        JOIN income_category c ON r.category_id = c.id
                        WHERE r.id = ?
                        """,
                (rs, rowNum) -> new FinanceRecordVO(
                        rs.getLong("id"),
                        rs.getString("record_no"),
                        rs.getLong("dept_id"),
                        rs.getString("dept_name"),
                        rs.getLong("account_id"),
                        rs.getString("account_name"),
                        rs.getLong("category_id"),
                        rs.getString("category_name"),
                        rs.getBigDecimal("amount"),
                        rs.getDate("occurred_on").toLocalDate(),
                        rs.getString("operator_name"),
                        rs.getString("remark"),
                        rs.getInt("status")
                ), id);
        if (records.isEmpty()) {
            throw new BusinessException("收入记录不存在");
        }
        return records.get(0);
    }

    private FinanceRecordVO getExpenseRecordVO(Long id) {
        List<FinanceRecordVO> records = jdbcTemplate.query("""
                        SELECT r.id, r.record_no, r.dept_id, d.dept_name, r.account_id, a.account_name,
                               r.category_id, c.category_name, r.amount, r.occurred_on, r.operator_name, r.remark, r.status
                        FROM expense_record r
                        JOIN dept_info d ON r.dept_id = d.id
                        JOIN account_info a ON r.account_id = a.id
                        JOIN expense_category c ON r.category_id = c.id
                        WHERE r.id = ?
                        """,
                (rs, rowNum) -> new FinanceRecordVO(
                        rs.getLong("id"),
                        rs.getString("record_no"),
                        rs.getLong("dept_id"),
                        rs.getString("dept_name"),
                        rs.getLong("account_id"),
                        rs.getString("account_name"),
                        rs.getLong("category_id"),
                        rs.getString("category_name"),
                        rs.getBigDecimal("amount"),
                        rs.getDate("occurred_on").toLocalDate(),
                        rs.getString("operator_name"),
                        rs.getString("remark"),
                        rs.getInt("status")
                ), id);
        if (records.isEmpty()) {
            throw new BusinessException("支出记录不存在");
        }
        return records.get(0);
    }

    private void validateIncomeRequest(FinanceRecordSaveRequest request) {
        departmentService.getEnabled(request.deptId());
        accountService.getEnabled(request.accountId());
        categoryService.getEnabledIncomeCategory(request.categoryId());
    }

    private void validateExpenseRequest(FinanceRecordSaveRequest request) {
        departmentService.getEnabled(request.deptId());
        accountService.getEnabled(request.accountId());
        categoryService.getEnabledExpenseCategory(request.categoryId());
    }

    private void fillIncomeRecord(IncomeRecord record, FinanceRecordSaveRequest request) {
        DeptInfo deptInfo = departmentService.getEnabled(request.deptId());
        AccountInfo accountInfo = accountService.getEnabled(request.accountId());
        IncomeCategory category = categoryService.getEnabledIncomeCategory(request.categoryId());
        record.setDeptId(deptInfo.getId());
        record.setAccountId(accountInfo.getId());
        record.setCategoryId(category.getId());
        record.setAmount(request.amount());
        record.setOccurredOn(request.occurredOn());
        record.setOperatorName(request.operatorName().trim());
        record.setRemark(request.remark());
        record.setStatus(request.status());
    }

    private void fillExpenseRecord(ExpenseRecord record, FinanceRecordSaveRequest request) {
        DeptInfo deptInfo = departmentService.getEnabled(request.deptId());
        AccountInfo accountInfo = accountService.getEnabled(request.accountId());
        ExpenseCategory category = categoryService.getEnabledExpenseCategory(request.categoryId());
        record.setDeptId(deptInfo.getId());
        record.setAccountId(accountInfo.getId());
        record.setCategoryId(category.getId());
        record.setAmount(request.amount());
        record.setOccurredOn(request.occurredOn());
        record.setOperatorName(request.operatorName().trim());
        record.setRemark(request.remark());
        record.setStatus(request.status());
    }

    private IncomeRecord getIncomeRecord(Long id) {
        IncomeRecord record = incomeRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("收入记录不存在");
        }
        return record;
    }

    private ExpenseRecord getExpenseRecord(Long id) {
        ExpenseRecord record = expenseRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("支出记录不存在");
        }
        return record;
    }

    private String generateRecordNo(String prefix, LocalDate occurredOn, boolean income) {
        String datePart = occurredOn.format(RECORD_DATE);
        String recordPrefix = prefix + datePart;
        long count = income
                ? incomeRecordMapper.selectCount(new LambdaQueryWrapper<IncomeRecord>().likeRight(IncomeRecord::getRecordNo, recordPrefix))
                : expenseRecordMapper.selectCount(new LambdaQueryWrapper<ExpenseRecord>().likeRight(ExpenseRecord::getRecordNo, recordPrefix));
        return recordPrefix + String.format("%03d", count + 1);
    }
}
