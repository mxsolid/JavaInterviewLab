# P00 Browser Network Summary

日期：2026-08-21
浏览器：Playwright CLI，Chromium，有头模式
前端：`http://127.0.0.1:5173`
后端：`http://127.0.0.1:8080`

## 页面与业务请求

| 页面 | 请求 | 状态 |
|---|---|---|
| `/` | `GET /api/dashboard` | 200 |
| `/` | `GET /api/study/wrong-questions` | 200 |
| `/` | `GET /api/study/reviews/due` | 200 |
| `/study` | `GET /api/study/plans` | 200 |
| `/study` | `GET /api/study/current-plan` | 200 |
| `/study` | `GET /api/study/today` | 200 |
| `/study` | `GET /api/study/plans/37` | 200 |
| `/questions` | `GET /api/categories` | 200 |
| `/questions` | `GET /api/topics` | 200 |
| `/questions` | `GET /api/questions?page=1&pageSize=10&status=ENABLED` | 200 |
| `/questions/1` | `GET /api/questions/1` | 200 |
| `/questions/1` | `GET /api/study/favorites` | 200 |
| `/questions/1` | `GET /api/study/questions/1/progress` | 200 |
| `/questions/1` | `GET /api/study/notes?targetType=QUESTION&targetId=1` | 200，两次 |
| `/review` | `GET /api/study/reviews/due` | 200 |
| `/review` | `GET /api/study/wrong-questions` | 200 |
| `/review` | `GET /api/study/favorites` | 200 |

本轮页面导航中未观察到业务请求 4xx/5xx。

## Console

- `[antd: Space] direction is deprecated. Please use orientation instead.`
- `[antd: List] The List component is deprecated.`
- `[antd: Alert] message is deprecated. Please use title instead.`

三类信息由 Vite 开发环境记录为 `console.error`。P00 只复现和记录，不修改组件。

## Smoke

- `GET /actuator/health`：200，`status=UP`。
- Spring Boot 启动日志确认 PostgreSQL 16.15、Flyway V10，11 个 migration 校验通过。
