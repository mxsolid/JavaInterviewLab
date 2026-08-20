# P06 验收记录

日期：2026-08-21

## 数据与契约

- Flyway：V16。
- 场景：12 个启用场景，作答写入 `scenario_attempt`，`(profile_id, client_attempt_id)` 幂等。
- Source：3 个项目自写教学片段，均关联真实 Topic。
- Lab：5 个启用定义，算法分别为 `BPLUS_TREE_INSERT`、`LRU_CACHE`、`HASHMAP_RESIZE`、`REDIS_REHASH`、`THREAD_POOL_SUBMIT`。
- Interview：会话、轮次均持久化；`(session_id, client_turn_id)` 幂等；四维权重为 40/25/20/15；provider 默认关闭。

## 自动验证

- `mvn -B -ntp test`：46/46 通过。
- `mvn -B -ntp package -DskipTests`：通过。
- `npm run typecheck`：通过。
- `npm run test`：5/5 通过。
- `npm run build`：通过。
- `npm run e2e`：12/12 通过。
- 浏览器采集 console error 0、page error 0、非预期 4xx/5xx 0。

## 人工验证

- Scenario：切换 Case、提交回答、显示持久化结果和参考主线。
- Source：切换代码行、显示注释、进入关联题库。
- Lab：五个实验均可切换，前后步骤可重放。
- Interview：文本回答、四维解释评分、结束汇总可用；无语音能力时不影响文本链路。
- 1720 桌面与 390 移动截图无内容遮挡和非预期横向溢出。

## 截图

- `screenshots/scenario-1720.png`
- `screenshots/scenario-mobile-390.png`
- `screenshots/source-1720.png`
- `screenshots/lab-1720.png`
- `screenshots/interview-1720.png`
- `screenshots/interview-mobile-390.png`
