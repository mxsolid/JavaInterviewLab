-- 收藏是当前用户偏好，不是答题历史；允许取消，不需要保留删除记录。
CREATE TABLE IF NOT EXISTS favorite (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES profile(id),
    target_type VARCHAR(32) NOT NULL,
    target_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (profile_id, target_type, target_id)
);

-- 收藏列表固定按用户、目标类型和创建时间倒序读取。
CREATE INDEX IF NOT EXISTS idx_favorite_profile_type_created
ON favorite (profile_id, target_type, created_at DESC);
