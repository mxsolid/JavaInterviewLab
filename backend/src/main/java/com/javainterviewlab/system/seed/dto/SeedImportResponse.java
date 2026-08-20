package com.javainterviewlab.system.seed.dto;

public record SeedImportResponse(String seedPack, String version, int created, int skipped) {
}
