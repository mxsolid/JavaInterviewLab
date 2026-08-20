package com.javainterviewlab.system.seed.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.system.seed.dto.SeedImportResponse;
import com.javainterviewlab.system.seed.service.SeedImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 种子只允许由调用方上传，服务端不接受任意文件系统路径。 */
@Tag(name = "系统", description = "本地题库种子导入")
@RestController
@RequestMapping("/api/system/seeds")
public class SeedImportController {

    private final SeedImportService seedImportService;

    public SeedImportController(SeedImportService seedImportService) {
        this.seedImportService = seedImportService;
    }

    /** 上传 JSON 后整批导入；结构错误或任一数据错误都会回滚。 */
    @Operation(summary = "导入题库种子", description = "上传 JSON 文件，按 seedPack、version 和 externalKey 幂等导入。")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<SeedImportResponse> importSeed(@RequestPart("file") MultipartFile file) {
        return ApiResponse.success(seedImportService.importJson(file));
    }
}
