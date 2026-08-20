-- 面试会话固定关联一道数据库题目，避免前端或规则评分器使用伪造 prompt。
ALTER TABLE interview_session
    ADD COLUMN question_id BIGINT REFERENCES question(id);

-- 四个维度分别持久化，结束会话时可解释汇总，不只保存一个总分。
ALTER TABLE interview_turn
    ADD COLUMN accuracy_score NUMERIC(5, 2),
    ADD COLUMN completeness_score NUMERIC(5, 2),
    ADD COLUMN depth_score NUMERIC(5, 2),
    ADD COLUMN expression_score NUMERIC(5, 2);

CREATE INDEX idx_interview_turn_session_sequence
    ON interview_turn (session_id, sequence_no, id);

-- P03 的 B+Tree search 元数据不满足 P06 insert/split 演示，保留记录但停止对外启用。
UPDATE lab_definition
SET status = 'DISABLED', updated_at = CURRENT_TIMESTAMP
WHERE code = 'bplus-tree-search';

INSERT INTO lab_definition (
    code, title, description, algorithm, version_label, initial_dataset, config_json, sort_order
) VALUES
    ('bplus-tree-insert', 'B+ 树插入与分裂', '观察键值有序插入、叶子节点分裂和分隔键向父节点传播。',
     'BPLUS_TREE_INSERT', 'PostgreSQL 16 教学抽象', '{"order":4,"keys":[3,8,12,17,21,30]}'::jsonb,
     '{"insertKey":28,"maxSteps":30}'::jsonb, 5),
    ('lru-cache', 'LRU 缓存淘汰', '观察 get/put 如何更新访问顺序，以及容量满时淘汰最久未使用项。',
     'LRU_CACHE', '通用算法', '{"capacity":3,"entries":[["A",1],["B",2],["C",3]]}'::jsonb,
     '{"operations":["GET:B","PUT:D:4"],"maxSteps":20}'::jsonb, 15),
    ('redis-rehash', 'Redis 渐进式 rehash', '观察每次字典操作迁移少量桶，避免一次性搬迁阻塞请求。',
     'REDIS_REHASH', 'Redis 7 教学抽象', '{"oldSize":4,"newSize":8,"buckets":[["A"],["B","E"],[],["D"]]}'::jsonb,
     '{"bucketsPerStep":1,"maxSteps":20}'::jsonb, 25)
ON CONFLICT (code) DO UPDATE
SET title = EXCLUDED.title,
    description = EXCLUDED.description,
    algorithm = EXCLUDED.algorithm,
    version_label = EXCLUDED.version_label,
    initial_dataset = EXCLUDED.initial_dataset,
    config_json = EXCLUDED.config_json,
    status = 'ENABLED',
    sort_order = EXCLUDED.sort_order,
    updated_at = CURRENT_TIMESTAMP;
