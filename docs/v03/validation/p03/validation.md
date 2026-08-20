# V0.3 P03 验证记录

## 数据与迁移

- Flyway：V12 新增 Scenario、Source、Lab、Interview 持久化结构；V13 兼容旧基线 `scenario.difficulty`。
- 场景包：`v03-scenarios-complete/2026.08.21.1`。
- 原始文件 SHA-256：`1cceed37c95563614a3ac55b544d415a52806e111ad38c92085c5e3fb1ef7461`。
- 导入结果：12 个场景、60 个 Case、12 个全局方案、140 个 Case/方案关系。
- 12 个方案在全部场景中的 code、name、principle 定义一致，因此按全局方案词典保存，避免 144 条重复记录。
- `scenario_attempt` 仅追加；`profile_id + client_attempt_id` 唯一约束负责并发幂等。

## 自动验证

- `mvn -B -ntp -Dtest=ScenarioContentIntegrationTest test`：3/3 通过。
- `mvn -B -ntp test`：40/40 通过，0 failure，0 error。
- `mvn -B -ntp package -DskipTests`：通过。
- 集成测试覆盖场景列表/详情/矩阵、Source 列表/详情、Lab 列表/详情、OpenAPI 路径、重复导入和 attempt 幂等。

## 手工 HTTP 验证

- JAR 运行端口：18080；`/actuator/health` 返回 `UP`。
- 场景列表 12 条；抽查详情为 5 个 Case、12 个方案，矩阵 15 个数据库关系单元。
- Source 3 条，Lab 3 条。
- 同一 `clientAttemptId` 连续提交：首次 `duplicated=false`，第二次 `duplicated=true`。
- 验证完成后已正常关闭服务。

## 边界

- Source 只保存本项目自写教学伪代码，不复制第三方大段源码。
- Interview 本阶段只建立表与 `InterviewEvaluator` 接口，不接外部 LLM，不返回伪造评分。
