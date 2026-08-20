-- review_task 保存未来复习计划及已完成历史，不能放进 progress 覆盖掉旧任务。
CREATE TABLE IF NOT EXISTS review_task (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES profile(id),
    question_id BIGINT NOT NULL REFERENCES question(id),
    due_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ
);

-- 旧 baseline 草案已有 review_task，但没有完成时间；保留原表和历史行，只补本版字段。
ALTER TABLE review_task ADD COLUMN IF NOT EXISTS completed_at TIMESTAMPTZ;

-- 同一道题可以有历史任务，但任意时刻最多一个待复习任务。
CREATE UNIQUE INDEX IF NOT EXISTS uk_review_task_pending
ON review_task (profile_id, question_id)
WHERE status = 'PENDING';

-- 今日待复习列表按档案、状态和到期时间过滤。
CREATE INDEX IF NOT EXISTS idx_review_task_profile_pending_due
ON review_task (profile_id, due_at)
WHERE status = 'PENDING';
