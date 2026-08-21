# P08 Keyword Search EXPLAIN ANALYZE

- 数据库：隔离 E2E 数据库 `jil_e2e`
- 数据规模：336 道正式题目
- 关键词：`HashMap`
- 执行时间：2026-08-21

## 等价生产查询

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT q.id,
       q.topic_id,
       t.name,
       c.id,
       c.name,
       q.title,
       q.star_level,
       q.difficulty,
       q.frequency_level,
       q.status,
       q.one_liner,
       q.version
FROM question q
JOIN topic t ON t.id = q.topic_id
JOIN category c ON c.id = t.category_id
WHERE q.title ILIKE '%HashMap%'
   OR q.one_liner ILIKE '%HashMap%'
ORDER BY q.updated_at DESC, q.id DESC
LIMIT 20 OFFSET 0;
```

## 执行计划摘要

```text
Limit  (actual time=0.654..0.656 rows=7 loops=1)
  -> Sort
       Sort Method: quicksort  Memory: 28kB
       -> Nested Loop
            -> Hash Join
                 -> Seq Scan on question q
                      (actual time=0.052..0.599 rows=7 loops=1)
                      Rows Removed by Filter: 329
                      Buffers: shared hit=56
                 -> Seq Scan on topic t
                      (actual rows=28 loops=1)
            -> Index Scan using category_pkey on category c
                 (loops=7)
Planning Time: 0.683 ms
Execution Time: 0.700 ms
```

## 结论

336 行题库下顺序扫描只读取 56 个已缓存页，完整查询为 0.700 ms。当前数据规模增加 trigram 索引只会增加迁移和写入成本，因此 P08 不调整生产索引；后续题库增长后再以相同查询复测。
