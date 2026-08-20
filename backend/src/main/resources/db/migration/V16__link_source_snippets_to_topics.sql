-- Source 关联专题由数据库维护，前端只按 topic_id 跳转题库，不根据标题猜测关系。
UPDATE source_snippet
SET topic_id = (SELECT id FROM topic WHERE code = 'topic-collections'),
    updated_at = CURRENT_TIMESTAMP
WHERE external_key = 'source-hashmap-bucket' AND topic_id IS NULL;

UPDATE source_snippet
SET topic_id = (SELECT id FROM topic WHERE code = 'topic-concurrency'),
    updated_at = CURRENT_TIMESTAMP
WHERE external_key = 'source-aqs-acquire' AND topic_id IS NULL;

UPDATE source_snippet
SET topic_id = (SELECT id FROM topic WHERE code = 'topic-db-transaction'),
    updated_at = CURRENT_TIMESTAMP
WHERE external_key = 'source-transaction-boundary' AND topic_id IS NULL;
