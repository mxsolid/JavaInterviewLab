package com.javainterviewlab.integration;

import com.javainterviewlab.JavaInterviewLabApplication;
import com.javainterviewlab.system.seed.dto.SeedImportResponse;
import com.javainterviewlab.system.seed.service.SeedImportService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/** 在隔离 PostgreSQL 数据库中验证完整 Flyway 与生产种子，绝不清理或改写 devdb。 */
class FreshDatabaseAcceptanceTest {

    private static final String ADMIN_URL = "jdbc:postgresql://127.0.0.1:5432/devdb";
    private static final String DATABASE_URL_PREFIX = "jdbc:postgresql://127.0.0.1:5432/";
    private static final String USERNAME = "dev";
    private static final String PASSWORD = "123456";
    private static final String DATABASE_PREFIX = "jil_p07_";
    private static final Pattern SAFE_DATABASE_NAME = Pattern.compile("jil_p07_[a-f0-9]{16}");

    @Test
    void shouldMigrateAndImportProductionSeedsIntoFreshDatabase() throws Exception {
        String databaseName = DATABASE_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, 16)
                .toLowerCase(Locale.ROOT);
        assertThat(SAFE_DATABASE_NAME.matcher(databaseName).matches()).isTrue();

        createDatabase(databaseName);
        ConfigurableApplicationContext context = null;
        try {
            context = new SpringApplicationBuilder(JavaInterviewLabApplication.class)
                    .web(WebApplicationType.NONE)
                    .profiles("local")
                    .properties(
                            "spring.main.banner-mode=off",
                            "logging.level.root=WARN"
                    )
                    .run(
                            "--spring.datasource.url=" + DATABASE_URL_PREFIX + databaseName,
                            "--spring.datasource.username=" + USERNAME,
                            "--spring.datasource.password=" + PASSWORD
                    );

            SeedImportService seedImportService = context.getBean(SeedImportService.class);
            ClassPathResource seed = new ClassPathResource("seed/v03-core-complete.json");
            SeedImportResponse response = seedImportService.importJson(new MockMultipartFile(
                    "file",
                    "v03-core-complete.json",
                    "application/json",
                    seed.getInputStream()
            ));

            JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
            assertThat(response.created()).isEqualTo(336);
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM question WHERE seed_pack = 'v03-core-complete'",
                    Integer.class
            )).isEqualTo(336);
            assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM scenario WHERE status = 'ENABLED'", Integer.class)).isEqualTo(12);
            assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM scenario_case", Integer.class)).isEqualTo(60);
            assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM study_plan_item", Integer.class)).isEqualTo(34);
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM source_snippet WHERE status = 'ENABLED' AND topic_id IS NOT NULL",
                    Integer.class
            )).isEqualTo(3);
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT version::integer FROM flyway_schema_history WHERE success ORDER BY installed_rank DESC LIMIT 1",
                    Integer.class
            )).isEqualTo(16);
        } finally {
            if (context != null) {
                context.close();
            }
            dropDatabase(databaseName);
        }
    }

    private void createDatabase(String databaseName) throws SQLException {
        verifyDatabaseName(databaseName);
        try (Connection connection = DriverManager.getConnection(ADMIN_URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement()) {
            connection.setAutoCommit(true);
            statement.executeUpdate("CREATE DATABASE \"" + databaseName + "\"");
        }
    }

    private void dropDatabase(String databaseName) throws SQLException {
        verifyDatabaseName(databaseName);
        try (Connection connection = DriverManager.getConnection(ADMIN_URL, USERNAME, PASSWORD)) {
            connection.setAutoCommit(true);
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = ? AND pid <> pg_backend_pid()"
            )) {
                statement.setString(1, databaseName);
                statement.execute();
            }
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DROP DATABASE \"" + databaseName + "\"");
            }
        }
    }

    private void verifyDatabaseName(String databaseName) {
        if (!SAFE_DATABASE_NAME.matcher(databaseName).matches()) {
            throw new IllegalArgumentException("拒绝操作非 P07 临时数据库：" + databaseName);
        }
    }
}
