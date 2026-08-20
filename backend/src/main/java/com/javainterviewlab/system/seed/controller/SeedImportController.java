package com.javainterviewlab.system.seed.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.system.seed.dto.SeedImportResponse;
import com.javainterviewlab.system.seed.dto.SeedValidationResponse;
import com.javainterviewlab.system.seed.service.SeedImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 种子只允许由调用方上传，服务端不接受任意文件系统路径。 */
@Tag(name = "系统", description = "本地题库种子导入")
@RestController
@RequestMapping({"/api/system/seeds", "/api/v1/system/seeds"})
public class SeedImportController {

    private final SeedImportService seedImportService;

    public SeedImportController(SeedImportService seedImportService) {
        this.seedImportService = seedImportService;
    }

    /** 解析并结合当前数据库计算导入决策，不写入任何内容。 */
    @Operation(summary = "校验题库种子", description = "校验完整内容、引用、版本、checksum 和来源覆盖边界。")
    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<SeedValidationResponse> validateSeed(@RequestPart("file") MultipartFile file) {
        return ApiResponse.success(seedImportService.validate(file));
    }

    /** 上传 JSON 后整批处理；dryRun 与真实导入共用校验和数据库决策。 */
    @Operation(summary = "导入题库种子", description = "dryRun=true 只返回预期变更；false 时在单事务内完成导入。")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<SeedImportResponse> importSeed(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean dryRun
    ) {
        return ApiResponse.success(seedImportService.importJson(file, dryRun));
    }
}
