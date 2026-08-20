package com.javainterviewlab.system.seed.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.system.seed.dto.SeedImportResponse;
import com.javainterviewlab.system.seed.service.SeedImportService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/system/seeds")
public class SeedImportController {
    private final SeedImportService seedImportService;
    public SeedImportController(SeedImportService seedImportService) { this.seedImportService=seedImportService; }
    @PostMapping("/import")
    public ApiResponse<SeedImportResponse> importSeed(@RequestParam String path) { return ApiResponse.success(seedImportService.importJson(new FileSystemResource(path))); }
}
