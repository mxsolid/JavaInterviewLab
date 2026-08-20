import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/** 对正式导入后的核心题库执行只读计数和 30 题稳定随机抽样。 */
public class SeedBankSpotCheck {

    private static final String SEED_PACK = "v03-core-complete";
    private static final String CHECKSUM = "a1f91d51d5fe1dbc687770bc3d88a0822eae4f1210327741599b5a56f301a5ec";
    private static final PrintWriter OUTPUT = new PrintWriter(
            new OutputStreamWriter(new FileOutputStream(FileDescriptor.out), StandardCharsets.UTF_8),
            true
    );

    public static void main(String[] args) throws Exception {
        String url = requiredEnvironment("POSTGRES_URL");
        String user = requiredEnvironment("POSTGRES_USER");
        String password = requiredEnvironment("POSTGRES_PASSWORD");
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            printSummary(connection);
            printSample(connection);
            printIndexPlan(connection);
        }
    }

    private static void printSummary(Connection connection) throws Exception {
        String sql = """
                SELECT COUNT(*) AS question_count,
                       COUNT(*) FILTER (WHERE star_level = 5) AS star5_count,
                       COUNT(*) FILTER (WHERE frequency_level = 'VERY_HIGH') AS very_high_count,
                       COUNT(*) FILTER (WHERE common_mistakes IS NULL OR score_points IS NULL
                           OR source_version IS NULL) AS incomplete_count,
                       (SELECT COUNT(*) FROM question_answer a JOIN question x ON x.id = a.question_id
                           WHERE x.seed_pack = ?) AS answer_count,
                       (SELECT COUNT(*) FROM question_follow_up f JOIN question x ON x.id = f.question_id
                           WHERE x.seed_pack = ?) AS follow_up_count,
                       (SELECT COUNT(*) FROM question_tag qt JOIN question x ON x.id = qt.question_id
                           WHERE x.seed_pack = ?) AS tag_relation_count
                FROM question
                WHERE seed_pack = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 1; index <= 4; index++) {
                statement.setString(index, SEED_PACK);
            }
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                long questionCount = result.getLong("question_count");
                long star5Count = result.getLong("star5_count");
                long veryHighCount = result.getLong("very_high_count");
                long incompleteCount = result.getLong("incomplete_count");
                long answerCount = result.getLong("answer_count");
                long followUpCount = result.getLong("follow_up_count");
                long tagRelationCount = result.getLong("tag_relation_count");
                if (questionCount != 336 || star5Count != 180 || veryHighCount != 90
                        || incompleteCount != 0 || answerCount != 1008 || followUpCount != 1008) {
                    throw new IllegalStateException("核心题库计数不符合 MANIFEST 或完整字段要求");
                }
                OUTPUT.printf(
                        "SUMMARY,questions=%d,star5=%d,veryHigh=%d,incomplete=%d,answers=%d,followUps=%d,tags=%d%n",
                        questionCount, star5Count, veryHighCount, incompleteCount,
                        answerCount, followUpCount, tagRelationCount
                );
            }
        }
    }

    private static void printSample(Connection connection) throws Exception {
        String sql = """
                SELECT q.external_key, q.title,
                       CASE WHEN q.one_liner IS NOT NULL AND q.plain_explanation IS NOT NULL
                           AND q.design_reason IS NOT NULL AND q.common_mistakes IS NOT NULL
                           AND q.score_points IS NOT NULL AND q.source_version IS NOT NULL
                           THEN TRUE ELSE FALSE END AS full_fields,
                       (SELECT COUNT(*) FROM question_answer a WHERE a.question_id = q.id) AS answers,
                       (SELECT COUNT(*) FROM question_follow_up f WHERE f.question_id = q.id) AS follow_ups,
                       (SELECT COUNT(*) FROM question_tag qt WHERE qt.question_id = q.id) AS tags
                FROM question q
                WHERE q.seed_pack = ?
                ORDER BY md5(q.external_key || ?)
                LIMIT 30
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, SEED_PACK);
            statement.setString(2, CHECKSUM);
            try (ResultSet result = statement.executeQuery()) {
                OUTPUT.println("externalKey,title,fullFields,answers,followUps,tags");
                int count = 0;
                while (result.next()) {
                    boolean fullFields = result.getBoolean("full_fields");
                    int answers = result.getInt("answers");
                    int followUps = result.getInt("follow_ups");
                    int tags = result.getInt("tags");
                    if (!fullFields || answers != 3 || followUps < 3 || tags == 0) {
                        throw new IllegalStateException("抽样题目内容不完整：" + result.getString("external_key"));
                    }
                    OUTPUT.printf(
                            "%s,%s,%s,%d,%d,%d%n",
                            csv(result.getString("external_key")),
                            csv(result.getString("title")),
                            fullFields,
                            answers,
                            followUps,
                            tags
                    );
                    count++;
                }
                if (count != 30) {
                    throw new IllegalStateException("核心题库不足 30 道，无法完成抽样");
                }
            }
        }
    }

    private static void printIndexPlan(Connection connection) throws Exception {
        connection.setAutoCommit(false);
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET LOCAL enable_seqscan = off");
            String sql = """
                    EXPLAIN SELECT q.id
                    FROM question q
                    WHERE q.topic_id = (SELECT id FROM topic WHERE code = 'topic-java-language')
                      AND q.status = 'ENABLED'
                    ORDER BY q.updated_at DESC, q.id DESC
                    LIMIT 20
                    """;
            StringBuilder plan = new StringBuilder();
            try (ResultSet result = statement.executeQuery(sql)) {
                while (result.next()) {
                    plan.append(result.getString(1)).append(System.lineSeparator());
                }
            }
            if (!plan.toString().contains("idx_question_topic_status_updated")) {
                throw new IllegalStateException("专题分页查询未使用 P02 复合索引");
            }
            OUTPUT.println("EXPLAIN,index=idx_question_topic_status_updated,used=true");
        } finally {
            connection.rollback();
            connection.setAutoCommit(true);
        }
    }

    private static String csv(String value) {
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("缺少环境变量：" + name);
        }
        return value;
    }
}
