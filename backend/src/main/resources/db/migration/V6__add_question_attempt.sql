-- 答题历史是不可变事实；学习进度和复习计划分别由后续任务维护。
-- 旧 baseline 草案可能已预建该表，本迁移只补齐 B02 需要的字段，不删除既有历史。
CREATE TABLE IF NOT EXISTS question_attempt (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES profile(id),
    question_id BIGINT NOT NULL REFERENCES question(id),
    client_attempt_id UUID,
    answer_text TEXT,
    viewed_answer BOOLEAN NOT NULL DEFAULT FALSE,
    self_rating SMALLINT CHECK (self_rating BETWEEN 1 AND 5),
    result_type VARCHAR(32),
    elapsed_ms BIGINT CHECK (elapsed_ms >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE question_attempt ADD COLUMN IF NOT EXISTS client_attempt_id UUID;
ALTER TABLE question_attempt ADD COLUMN IF NOT EXISTS result_type VARCHAR(32);

-- 历史草案记录没有客户端 UUID 和结果类型时，迁移为每行补一个稳定唯一值，随后才收紧新旧数据约束。
UPDATE question_attempt
SET client_attempt_id = md5(id::text || created_at::text)::uuid
WHERE client_attempt_id IS NULL;

UPDATE question_attempt
SET result_type = 'NOT_ANSWERED'
WHERE result_type IS NULL;

ALTER TABLE question_attempt ALTER COLUMN client_attempt_id SET NOT NULL;
ALTER TABLE question_attempt ALTER COLUMN result_type SET NOT NULL;
ALTER TABLE question_attempt DROP CONSTRAINT IF EXISTS ck_question_attempt_elapsed_ms;
ALTER TABLE question_attempt ADD CONSTRAINT ck_question_attempt_elapsed_ms
CHECK (elapsed_ms IS NULL OR elapsed_ms >= 0) NOT VALID;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint constraint_definition
        JOIN pg_class table_definition ON table_definition.oid = constraint_definition.conrelid
        WHERE table_definition.relname = 'question_attempt'
          AND constraint_definition.contype = 'u'
          AND pg_get_constraintdef(constraint_definition.oid) = 'UNIQUE (profile_id, client_attempt_id)'
    ) THEN
        ALTER TABLE question_attempt
        ADD CONSTRAINT uk_question_attempt_profile_client UNIQUE (profile_id, client_attempt_id);
    END IF;
END $$;

-- 支持后续按用户和题目查询最近练习历史，不为低频写入增加无关索引。
CREATE INDEX idx_attempt_profile_question_time
ON question_attempt (profile_id, question_id, created_at DESC);
