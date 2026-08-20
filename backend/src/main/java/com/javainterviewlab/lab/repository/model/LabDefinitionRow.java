package com.javainterviewlab.lab.repository.model;

/** Lab definition 数据库投影。 */
public record LabDefinitionRow(
        Long id,
        String code,
        String title,
        String description,
        String algorithm,
        String versionLabel,
        String initialDatasetJson,
        String configJson
) {
}
