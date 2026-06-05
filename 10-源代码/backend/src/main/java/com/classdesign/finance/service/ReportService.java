package com.classdesign.finance.service;

import com.classdesign.finance.vo.ReportSummaryVO;
import com.classdesign.finance.vo.StatItemVO;
import com.classdesign.finance.vo.TrendPointVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.temporal.ChronoUnit;

@Service
public class ReportService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    private final JdbcTemplate jdbcTemplate;

    public ReportService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ReportSummaryVO weekly(String week, Long deptId) {
        LocalDate baseDate = LocalDate.now();
        if (week != null && !week.isBlank() && week.matches("\\d{4}-W\\d{1,2}")) {
            String[] parts = week.split("-W");
            int year = Integer.parseInt(parts[0]);
            int weekNo = Integer.parseInt(parts[1]);
            baseDate = LocalDate.of(year, 1, 4)
                    .with(WeekFields.ISO.weekOfWeekBasedYear(), weekNo)
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }
        LocalDate start = baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = start.plusDays(6);
        String label = start.getYear() + "-W" + String.format("%02d", start.get(WeekFields.ISO.weekOfWeekBasedYear()));
        return buildSummary("weekly", label, deptId, start, end);
    }

    public ReportSummaryVO monthly(String month, Long deptId) {
        YearMonth yearMonth = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month, MONTH_FORMATTER);
        return buildSummary("monthly", yearMonth.toString(), deptId, yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    public ReportSummaryVO yearly(String year, Long deptId) {
        Year value = year == null || year.isBlank() ? Year.now() : Year.parse(year);
        return buildSummary("yearly", value.toString(), deptId,
                LocalDate.of(value.getValue(), Month.JANUARY, 1),
                LocalDate.of(value.getValue(), Month.DECEMBER, 31));
    }

    public ReportSummaryVO custom(String startDate, String endDate, Long deptId) {
        LocalDate start = startDate == null || startDate.isBlank() ? LocalDate.now().withDayOfMonth(1) : LocalDate.parse(startDate);
        LocalDate end = endDate == null || endDate.isBlank() ? LocalDate.now() : LocalDate.parse(endDate);
        return buildSummary("custom", start + "~" + end, deptId, start, end);
    }

    public ReportSummaryVO dashboard() {
        YearMonth currentMonth = YearMonth.now();
        return buildSummary("dashboard", currentMonth.toString(), null, currentMonth.atDay(1), currentMonth.atEndOfMonth());
    }

    private ReportSummaryVO buildSummary(String periodType, String periodValue, Long deptId, LocalDate start, LocalDate end) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("periodType", periodType);
        query.put("periodValue", periodValue);
        query.put("deptId", deptId);
        query.put("startDate", start);
        query.put("endDate", end);

        double totalIncome = sumAmount("income_record", start, end, deptId);
        double totalExpense = sumAmount("expense_record", start, end, deptId);
        double balance = totalIncome - totalExpense;

        return new ReportSummaryVO(
                query,
                totalIncome,
                totalExpense,
                balance,
                buildCategoryStats(start, end, deptId),
                buildDepartmentStats(start, end, deptId),
                buildTrendStats(start, end, deptId)
        );
    }

    private double sumAmount(String tableName, LocalDate start, LocalDate end, Long deptId) {
        StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(amount), 0) FROM " + tableName + " WHERE status = 1 AND occurred_on BETWEEN ? AND ? ");
        List<Object> params = new ArrayList<>(List.of(Date.valueOf(start), Date.valueOf(end)));
        if (deptId != null) {
            sql.append(" AND dept_id = ? ");
            params.add(deptId);
        }
        Double value = jdbcTemplate.queryForObject(sql.toString(), Double.class, params.toArray());
        return value == null ? 0D : value;
    }

    private List<StatItemVO> buildCategoryStats(LocalDate start, LocalDate end, Long deptId) {
        String incomeCondition = deptId == null ? "" : " AND r.dept_id = ? ";
        String expenseCondition = deptId == null ? "" : " AND r.dept_id = ? ";
        String sql = """
                SELECT c.category_name AS name, SUM(r.amount) AS amount
                FROM income_record r
                JOIN income_category c ON r.category_id = c.id
                WHERE r.status = 1 AND r.occurred_on BETWEEN ? AND ? %s
                GROUP BY c.category_name
                UNION ALL
                SELECT c.category_name AS name, SUM(r.amount) AS amount
                FROM expense_record r
                JOIN expense_category c ON r.category_id = c.id
                WHERE r.status = 1 AND r.occurred_on BETWEEN ? AND ? %s
                GROUP BY c.category_name
                ORDER BY amount DESC
                """.formatted(incomeCondition, expenseCondition);
        List<Object> params = new ArrayList<>(List.of(Date.valueOf(start), Date.valueOf(end)));
        if (deptId != null) {
            params.add(deptId);
        }
        params.add(Date.valueOf(start));
        params.add(Date.valueOf(end));
        if (deptId != null) {
            params.add(deptId);
        }
        return jdbcTemplate.query(sql, (rs, rowNum) -> new StatItemVO(rs.getString("name"), rs.getDouble("amount")), params.toArray());
    }

    private List<StatItemVO> buildDepartmentStats(LocalDate start, LocalDate end, Long deptId) {
        String deptCondition = deptId == null ? "" : " WHERE d.id = ? ";
        String sql = """
                SELECT d.dept_name AS name,
                       COALESCE(i.total_income, 0) - COALESCE(e.total_expense, 0) AS amount
                FROM dept_info d
                LEFT JOIN (
                    SELECT dept_id, SUM(amount) AS total_income
                    FROM income_record
                    WHERE status = 1 AND occurred_on BETWEEN ? AND ?
                    GROUP BY dept_id
                ) i ON d.id = i.dept_id
                LEFT JOIN (
                    SELECT dept_id, SUM(amount) AS total_expense
                    FROM expense_record
                    WHERE status = 1 AND occurred_on BETWEEN ? AND ?
                    GROUP BY dept_id
                ) e ON d.id = e.dept_id
                %s
                ORDER BY d.id ASC
                """.formatted(deptCondition);
        List<Object> params = new ArrayList<>(List.of(Date.valueOf(start), Date.valueOf(end), Date.valueOf(start), Date.valueOf(end)));
        if (deptId != null) {
            params.add(deptId);
        }
        return jdbcTemplate.query(sql, (rs, rowNum) -> new StatItemVO(rs.getString("name"), rs.getDouble("amount")), params.toArray());
    }

    private List<TrendPointVO> buildTrendStats(LocalDate start, LocalDate end, Long deptId) {
        boolean fullYear = start.getDayOfYear() == 1 && end.getDayOfYear() == end.lengthOfYear();
        boolean monthlyBucket = fullYear || ChronoUnit.DAYS.between(start, end) > 62;
        return monthlyBucket ? buildMonthlyTrend(start, end, deptId) : buildDailyTrend(start, end, deptId);
    }

    private List<TrendPointVO> buildDailyTrend(LocalDate start, LocalDate end, Long deptId) {
        Map<LocalDate, Double> incomeMap = sumByDay("income_record", start, end, deptId);
        Map<LocalDate, Double> expenseMap = sumByDay("expense_record", start, end, deptId);
        List<TrendPointVO> result = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            result.add(new TrendPointVO(
                    date.format(DAY_FORMATTER),
                    incomeMap.getOrDefault(date, 0D),
                    expenseMap.getOrDefault(date, 0D)
            ));
        }
        return result;
    }

    private List<TrendPointVO> buildMonthlyTrend(LocalDate start, LocalDate end, Long deptId) {
        Map<String, Double> incomeMap = sumByMonth("income_record", start, end, deptId);
        Map<String, Double> expenseMap = sumByMonth("expense_record", start, end, deptId);
        List<TrendPointVO> result = new ArrayList<>();
        for (YearMonth current = YearMonth.from(start); !current.isAfter(YearMonth.from(end)); current = current.plusMonths(1)) {
            String key = current.toString();
            result.add(new TrendPointVO(
                    key,
                    incomeMap.getOrDefault(key, 0D),
                    expenseMap.getOrDefault(key, 0D)
            ));
        }
        return result;
    }

    private Map<LocalDate, Double> sumByDay(String tableName, LocalDate start, LocalDate end, Long deptId) {
        StringBuilder sql = new StringBuilder("SELECT occurred_on, SUM(amount) AS amount FROM " + tableName
                + " WHERE status = 1 AND occurred_on BETWEEN ? AND ? ");
        List<Object> params = new ArrayList<>(List.of(Date.valueOf(start), Date.valueOf(end)));
        if (deptId != null) {
            sql.append(" AND dept_id = ? ");
            params.add(deptId);
        }
        sql.append(" GROUP BY occurred_on ");
        return jdbcTemplate.query(sql.toString(), rs -> {
            Map<LocalDate, Double> result = new LinkedHashMap<>();
            while (rs.next()) {
                result.put(rs.getDate("occurred_on").toLocalDate(), rs.getDouble("amount"));
            }
            return result;
        }, params.toArray());
    }

    private Map<String, Double> sumByMonth(String tableName, LocalDate start, LocalDate end, Long deptId) {
        StringBuilder sql = new StringBuilder("SELECT DATE_FORMAT(occurred_on, '%Y-%m') AS month_label, SUM(amount) AS amount FROM "
                + tableName + " WHERE status = 1 AND occurred_on BETWEEN ? AND ? ");
        List<Object> params = new ArrayList<>(List.of(Date.valueOf(start), Date.valueOf(end)));
        if (deptId != null) {
            sql.append(" AND dept_id = ? ");
            params.add(deptId);
        }
        sql.append(" GROUP BY DATE_FORMAT(occurred_on, '%Y-%m') ORDER BY month_label ");
        return jdbcTemplate.query(sql.toString(), rs -> {
            Map<String, Double> result = new LinkedHashMap<>();
            while (rs.next()) {
                result.put(rs.getString("month_label"), rs.getDouble("amount"));
            }
            return result;
        }, params.toArray());
    }
}
