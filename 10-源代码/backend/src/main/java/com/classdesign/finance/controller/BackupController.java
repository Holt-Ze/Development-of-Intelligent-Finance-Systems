package com.classdesign.finance.controller;

import com.classdesign.finance.common.ApiResponse;
import com.classdesign.finance.dto.BackupCreateRequest;
import com.classdesign.finance.service.BackupService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/backups")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.success(backupService.list());
    }

    @PostMapping
    public ApiResponse<?> create(@RequestBody(required = false) BackupCreateRequest request) {
        return ApiResponse.success(backupService.createBackup(request));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Resource resource = backupService.download(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(resource.getFilename())
                        .build().toString())
                .body(resource);
    }

    @PostMapping("/{id}/restore")
    public ApiResponse<?> restore(@PathVariable Long id) {
        return ApiResponse.success(backupService.restore(id));
    }
}
