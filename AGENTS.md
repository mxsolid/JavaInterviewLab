# AGENTS.md — Java Interview Lab 强制开发约束

本文件是所有 Coding Agent 的最高优先级项目约束。开始任何修改前必须完整阅读。

## 一、环境与编码

1. 操作系统：Windows 11。
2. PowerShell 7：`D:\Tools\PowerShell\7\pwsh.exe`。
3. 项目根目录：`D:\Develop\project\Ai\JavaInterviewLab`。
4. JDK 21：`D:\Develop\Java\jdk-21`。
5. Maven 位于：`D:\Develop\maven\apache-maven-3.8.4`。
6. 所有文本文件必须保持 UTF-8 编码。
7. PowerShell 处理中文前先：
   - `chcp 65001`
   - 设置 InputEncoding / OutputEncoding / `$OutputEncoding` 为 UTF-8。
8. PowerShell 读取中文文件使用：
   - `Get-Content -Encoding UTF8`
9. 禁止使用 sed / awk 修改或处理包含中文的文件。
10. 修改中文文本优先使用 Python、Node.js 或 PowerShell 7 的 UTF-8 API。
11. 修改已有文件时禁止无意义地改变换行、编码、格式。

## 二、代码语言规范

1. 代码注释必须使用简体中文。
2. 注释优先解释“为什么这样设计”，不要重复代码表面含义。
3. 核心业务、复杂条件、事务边界、状态转换、兼容性逻辑必须有中文说明。
4. 日志必须简洁清晰，不记录密码、Token、完整用户敏感数据。
5. 禁止 `System.out.println`，统一 SLF4J。
6. 禁止魔法数字、魔法字符串。
7. 业务状态使用 Enum；固定 Key、默认值、Header 名等使用常量或配置。
8. 禁止为“体现设计模式”而过度设计。只有真实存在变化点时才使用策略、模板、工厂、责任链等。
9. 每个设计模式使用处，应在代码附近用中文说明：
   - 变化点是什么；
   - 为什么该模式适合；
   - 不使用时会带来什么问题。
10. Controller 不写业务逻辑。
11. Service 不直接拼接 SQL。
12. DTO / VO / Entity 职责分离，不把数据库 Entity 直接作为 API 输出。
13. 异常必须统一转换为 API 错误格式。
14. 日期时间统一使用 `java.time`。
15. 数据库时间统一保存时间戳；前端展示时再格式化。默认日期时间格式 `yyyy-MM-dd HH:mm:ss`（`MM` 表示月，`mm` 表示分钟，`HH` 表示 24 小时制）。

## 三、架构原则

采用“按业务功能分包”，不要一开始拆微服务。

推荐：

```text
com.javainterviewlab
├─ common
│  ├─ api
│  ├─ exception
│  ├─ logging
│  └─ util
├─ content
│  ├─ category
│  ├─ topic
│  ├─ question
│  └─ tag
├─ study
│  ├─ progress
│  ├─ attempt
│  ├─ review
│  ├─ note
│  └─ favorite
├─ scenario
└─ system
```

每个功能内部可按：
`controller / service / repository / domain / dto / mapper`

不要套用重量级 DDD。

## 四、数据库规则

1. PostgreSQL 是 V0.1～V0.3 唯一事实源。
2. Redis 在这三个版本不是强依赖。
3. 所有表结构变更通过 Flyway migration。
4. 禁止启动时自动 `ddl-auto=create/update`。
5. 关键表包含：
   - `created_at`
   - `updated_at`
6. 用户可编辑数据增加 `version` 乐观锁字段。
7. 答题历史 `question_attempt` 采用追加写，不覆盖历史。
8. 学习进度 `study_progress` 与答题历史更新必须在同一事务中。
9. 删除内置题目默认软删除或禁用，不直接物理删除。
10. 初始化内容必须支持幂等导入。
11. 系统内置内容与用户自定义内容用 `origin_type` 区分：
    - BUILTIN
    - USER
    - IMPORTED
    - FUTURE_AI

## 五、前端 UI / UX

采用用户已确认的白色教育产品风格：
- 白色主背景
- 淡蓝 / 紫 / 青绿渐变
- 圆角卡片
- 克制阴影
- 高信息密度但不拥挤

必须使用统一 Design Token：
- 色彩
- 圆角
- 阴影
- 间距
- 字号
- 动画时长

禁止组件内部大量硬编码颜色。

核心复用组件：
- PageHeader
- StatCard
- StarRating
- DifficultyTag
- MasteryTag
- ContentCard
- QuestionAnswerTabs
- EmptyState
- LoadingState
- ErrorState
- MarkdownRenderer
- EnglishTermSpeaker
- ConfirmDialog

动画统一：
- 页面切换：轻量淡入 / 位移
- 卡片 hover：轻量
- 禁止大量无意义动画
- 后续 B+ 树等教学动画独立进入 animation 模块

## 六、内容讲解规范

任何 ★★★★★ 重要题目，内容字段至少支持：

1. 一句话理解
2. 通俗讲解
3. 为什么这样设计
4. 30 秒面试回答
5. 2～3 分钟标准回答
6. 深入原理
7. 代码示例（如果适合）
8. 常见错误 / 易错点
9. 面试得分点
10. 高频追问
11. 典型业务场景
12. 相关知识
13. 来源 / 版本说明

不要为了填满字段编造内容。

## 七、Agent 执行纪律

每次任务：
- 只修改任务范围内文件；
- 不顺手重构无关代码；
- 不升级无关依赖；
- 不删除不理解的代码；
- 不改变产品范围；
- 不自动实现下一版本；
- 发现文档冲突先记录到 `PROGRESS.md` 的 BLOCKED 区域；
- 能做合理默认决策时直接做，并记录原因；
- 每次修改后必须给出验证命令和结果。

## 八、完成标准

一个任务只有同时满足以下条件才可标记 DONE：
- 代码完成；
- 编译通过；
- 自动测试通过或明确记录暂缺；
- 核心接口手动验证；
- 中文注释满足规范；
- 无明显硬编码；
- `PROGRESS.md` 已更新；
- `CHANGELOG_DEV.md` 已更新。
