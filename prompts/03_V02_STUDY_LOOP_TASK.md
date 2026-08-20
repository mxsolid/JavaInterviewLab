# Agent Task：V0.2 学习闭环

按 B01 → B08 顺序。

最重要约束：
- PostgreSQL 是唯一事实源；
- question_attempt 只追加；
- study_progress 是当前快照；
- 提交练习必须事务；
- 笔记必须乐观锁；
- UI 显示保存状态。

掌握度枚举建议：
- UNKNOWN
- SEEN
- BASIC
- SOLID
- MASTERED

学习阶段建议：
- PREVIEW
- LEARNING
- PRACTICING
- REVIEWING
- COMPLETED

复习规则第一版保持简单：
- UNKNOWN / 答错：1 天
- SEEN：3 天
- BASIC：7 天
- SOLID：14 天
- MASTERED：30 天

规则集中在 ReviewPolicy，不要散落硬编码。
