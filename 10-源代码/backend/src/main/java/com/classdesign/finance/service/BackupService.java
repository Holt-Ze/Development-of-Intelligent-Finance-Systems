package com.classdesign.finance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classdesign.finance.common.BusinessException;
import com.classdesign.finance.dto.BackupCreateRequest;
import com.classdesign.finance.entity.BackupRecord;
import com.classdesign.finance.mapper.BackupRecordMapper;
import com.classdesign.finance.vo.BackupFileVO;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BackupService {

    private static final DateTimeFormatter FILE_NAME_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BackupRecordMapper backupRecordMapper;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.backup-dir}")
    private String backupDir;

    public BackupService(BackupRecordMapper backupRecordMapper, JdbcTemplate jdbcTemplate) {
        this.backupRecordMapper = backupRecordMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(Paths.get(backupDir));
    }

    public List<BackupFileVO> list() {
        return backupRecordMapper.selectList(new LambdaQueryWrapper<BackupRecord>().orderByDesc(BackupRecord::getCreatedAt))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Transactional
    public BackupFileVO createBackup(BackupCreateRequest request) {
        String fileName = "finance_backup_" + LocalDateTime.now().format(FILE_NAME_TIME) + ".sql";
        Path path = Paths.get(backupDir).resolve(fileName);
        try {
            Files.writeString(path, buildBackupContent(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new BusinessException("备份文件创建失败");
        }

        BackupRecord record = new BackupRecord();
        record.setBackupName(fileName);
        record.setFilePath(path.toAbsolutePath().toString());
        try {
            record.setFileSize(Files.size(path));
        } catch (IOException exception) {
            record.setFileSize(0L);
        }
        record.setCreatedBy(request == null || request.createdBy() == null || request.createdBy().isBlank() ? "系统管理员" : request.createdBy());
        record.setRemark(request == null ? null : request.remark());
        record.setCreatedAt(LocalDateTime.now());
        backupRecordMapper.insert(record);
        return toVO(record);
    }

    public Resource download(Long id) {
        BackupRecord record = getRecord(id);
        Path path = Paths.get(record.getFilePath());
        if (!Files.exists(path)) {
            throw new BusinessException("备份文件不存在");
        }
        return new FileSystemResource(path);
    }

    @Transactional
    public BackupFileVO restore(Long id) {
        BackupRecord record = getRecord(id);
        Path path = Paths.get(record.getFilePath());
        if (!Files.exists(path)) {
            throw new BusinessException("备份文件不存在");
        }
        try {
            executeSqlScript(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new BusinessException("备份文件读取失败");
        }
        record.setRestoredAt(LocalDateTime.now());
        backupRecordMapper.updateById(record);
        return toVO(record);
    }

    private BackupRecord getRecord(Long id) {
        BackupRecord record = backupRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("备份记录不存在");
        }
        return record;
    }

    private BackupFileVO toVO(BackupRecord record) {
        return new BackupFileVO(
                record.getId(),
                record.getBackupName(),
                record.getFilePath(),
                record.getFileSize(),
                record.getCreatedBy(),
                record.getCreatedAt() == null ? null : record.getCreatedAt().format(DISPLAY_TIME),
                record.getRestoredAt() == null ? null : record.getRestoredAt().format(DISPLAY_TIME),
                record.getRemark()
        );
    }

    private String buildBackupContent() {
        List<String> tables = List.of(
                "sys_role",
                "sys_user",
                "sys_user_role",
                "dept_info",
                "account_info",
                "income_category",
                "expense_category",
                "income_record",
                "expense_record",
                "backup_record"
        );
        StringBuilder builder = new StringBuilder();
        builder.append("USE finance_management_system;\n");
        builder.append("SET FOREIGN_KEY_CHECKS = 0;\n");
        List<String> deleteOrder = List.of(
                "sys_user_role",
                "income_record",
                "expense_record",
                "backup_record",
                "income_category",
                "expense_category",
                "account_info",
                "dept_info",
                "sys_user",
                "sys_role"
        );
        for (String table : deleteOrder) {
            builder.append("DELETE FROM ").append(table).append(";\n");
        }
        for (String table : tables) {
            appendInsertStatements(builder, table);
        }
        builder.append("SET FOREIGN_KEY_CHECKS = 1;\n");
        return builder.toString();
    }

    private void appendInsertStatements(StringBuilder builder, String tableName) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM " + tableName + " ORDER BY 1");
        for (Map<String, Object> row : rows) {
            List<String> columns = new ArrayList<>(row.keySet());
            List<String> values = columns.stream().map(column -> formatSqlValue(row.get(column))).toList();
            builder.append("INSERT INTO ")
                    .append(tableName)
                    .append(" (")
                    .append(String.join(", ", columns))
                    .append(") VALUES (")
                    .append(String.join(", ", values))
                    .append(");\n");
        }
    }

    private String formatSqlValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Number) {
            return value.toString();
        }
        return "'" + value.toString().replace("\\", "\\\\").replace("'", "''") + "'";
    }

    private void executeSqlScript(String script) {
        StringBuilder current = new StringBuilder();
        for (String rawLine : script.split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("--")) {
                continue;
            }
            current.append(line).append(' ');
            if (line.endsWith(";")) {
                String statement = current.toString().trim();
                if (statement.endsWith(";")) {
                    statement = statement.substring(0, statement.length() - 1);
                }
                jdbcTemplate.execute(statement);
                current.setLength(0);
            }
        }
    }
}
