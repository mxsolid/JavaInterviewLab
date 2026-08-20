# Master Agent Prompt

你正在开发本地项目 **Java Interview Lab**。

项目根目录：
`D:\Develop\project\Ai\JavaInterviewLab`

## 强制执行

开始前：
1. 完整读取 `AGENTS.md`。
2. 使用 PowerShell 7：`D:\Tools\PowerShell\7\pwsh.exe`。
3. 先执行 UTF-8 初始化：
   - `chcp 65001`
   - 设置 Console InputEncoding / OutputEncoding / `$OutputEncoding` 为 UTF-8。
4. 读取中文文件用 `Get-Content -Encoding UTF8`。
5. 禁止 sed / awk 处理中文文件。
6. 修改中文内容使用 Python / Node.js / PowerShell UTF-8 API。
7. 读取 `PROGRESS.md`。
8. 只执行我本次明确指定的一个 Task ID。

## 项目原则

- Java 21
- Spring Boot 3.5.x
- PostgreSQL
- React + TypeScript
- V0.1～V0.3 不强制 Redis
- 所有关键数据存 PostgreSQL
- 中文注释
- 注释解释“为什么”
- 无魔法数字 / 字符串
- Enum / Constant / Config
- 不过度设计
- 不提前做 AI / 动画 / 源码浏览器
- 前端使用统一白色教育产品主题
- 不引入无必要依赖
- 不修改任务外代码

## 代码质量

任何核心设计决策，请在实现旁用简体中文说明原因。
如果存在两种合理方案：
- 选择更简单、更容易维护、更适合当前 V0.1～V0.3 的方案；
- 在 `CHANGELOG_DEV.md` 简短记录取舍；
- 不为了未来可能需求构建复杂抽象。

## Task 执行流程

1. 将当前 Task 在 `PROGRESS.md` 标记 `IN_PROGRESS`。
2. 检查现有实现，避免重复造轮子。
3. 输出本任务的简短实现计划。
4. 编码。
5. 执行格式检查 / 编译 / 测试。
6. 若失败，定位并修复当前任务导致的问题。
7. 更新 `PROGRESS.md` 为 `DONE` 或 `BLOCKED`。
8. 更新 `CHANGELOG_DEV.md`：
   - 做了什么
   - 为什么这样做
   - 验证命令
   - 验证结果
9. 最终回复必须包含：
   - 修改文件列表
   - 核心设计说明
   - 测试结果
   - IDEA 中如何验证
   - 遗留问题
10. 完成后停止，不开始下一个 Task。
