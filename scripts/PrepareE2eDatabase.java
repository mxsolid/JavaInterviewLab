import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

/** 只允许重建固定 E2E 数据库，防止测试脚本误删开发库。 */
public class PrepareE2eDatabase {

    private static final String DATABASE_NAME = "jil_e2e";

    public static void main(String[] args) throws Exception {
        String adminUrl = requireEnvironment("E2E_DB_ADMIN_URL");
        String username = requireEnvironment("E2E_DB_USER");
        String password = requireEnvironment("E2E_DB_PASSWORD");
        try (Connection connection = DriverManager.getConnection(adminUrl, username, password)) {
            connection.setAutoCommit(true);
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT pg_terminate_backend(pid) FROM pg_stat_activity "
                            + "WHERE datname = ? AND pid <> pg_backend_pid()"
            )) {
                statement.setString(1, DATABASE_NAME);
                statement.execute();
            }
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DROP DATABASE IF EXISTS \"" + DATABASE_NAME + "\"");
                statement.executeUpdate("CREATE DATABASE \"" + DATABASE_NAME + "\"");
            }
        }
    }

    private static String requireEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("缺少环境变量：" + name);
        }
        return value;
    }
}
