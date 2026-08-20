# Agent Task：V0.3 场景训练

按 C01 → C09 顺序。

## 必须支持的数据结构

Scenario
  ├─ Case A
  ├─ Case B
  ├─ Case C
  └─ ...
      ├─ Solution 1
      ├─ Solution 2
      └─ Solution N

Solution 可在多个 Case 中复用。

每个 Case Solution 关系保存：
- applicability
- recommendationLevel
- isFinalDefense
- pros
- cons
- risks
- reason
- sortOrder

## 首个完整示范

`支付重复处理 ★★★★★`

Case A：用户疯狂点击支付按钮
Case B：支付平台重复回调
Case C：网关超时客户端自动重试
Case D：MQ 重复投递支付成功消息
Case E：两个实例同时处理一个支付通知

可比较：
- 请求幂等号
- 数据库唯一索引
- 状态机
- Redis SETNX（作为方案内容，不要求系统本身启 Redis）
- 分布式锁
- 消费幂等
- 支付流水唯一约束

页面必须解释：
- 为什么适合；
- 为什么不适合；
- 什么是最终数据防线；
- 哪些方案应该组合。

禁止把方案矩阵写死在前端。
