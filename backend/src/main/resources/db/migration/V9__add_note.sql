-- 笔记是每个档案对一个内容目标的当前文本，version 用于阻止两个页面静默互相覆盖。
CREATE TABLE IF NOT EXISTS note (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES profile(id),
    target_type VARCHAR(32) NOT NULL,
    target_id BIGINT NOT NULL,
    content TEXT NOT NULL DEFAULT '',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (profile_id, target_type, target_id)
);
