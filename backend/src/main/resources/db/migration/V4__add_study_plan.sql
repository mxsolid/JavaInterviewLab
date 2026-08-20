CREATE TABLE IF NOT EXISTS profile (
    id BIGSERIAL PRIMARY KEY,
    display_name VARCHAR(100) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_profile_default ON profile (is_default) WHERE is_default;

-- V0.2 暂无账户体系；保留默认 profile 让学习记录从一开始就有稳定归属。
INSERT INTO profile (display_name, is_default)
SELECT '本地学习者', TRUE
WHERE NOT EXISTS (SELECT 1 FROM profile WHERE is_default);

-- 学习路线是可调整的产品内容，不能写死在 React 页面中。
CREATE TABLE study_plan (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    duration_days INTEGER NOT NULL CHECK (duration_days > 0),
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE study_plan_day (
    id BIGSERIAL PRIMARY KEY,
    study_plan_id BIGINT NOT NULL REFERENCES study_plan(id) ON DELETE CASCADE,
    day_number INTEGER NOT NULL CHECK (day_number > 0),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (study_plan_id, day_number)
);

-- target_type + target_id 允许计划项后续指向 scenario；系统导入时解析真实 topic/question，避免把路线文本复制到前端。
CREATE TABLE study_plan_item (
    id BIGSERIAL PRIMARY KEY,
    study_plan_day_id BIGINT NOT NULL REFERENCES study_plan_day(id) ON DELETE CASCADE,
    target_type VARCHAR(32) NOT NULL CHECK (target_type IN ('TOPIC', 'QUESTION', 'SCENARIO')),
    target_id BIGINT NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (study_plan_day_id, target_type, target_id)
);

CREATE TABLE profile_plan (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES profile(id),
    study_plan_id BIGINT NOT NULL REFERENCES study_plan(id),
    started_at TIMESTAMPTZ NOT NULL,
    selected_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (profile_id, study_plan_id)
);

-- 一个 profile 同时只能有一条当前路线，路线切换时保留旧记录供后续统计。
CREATE UNIQUE INDEX uk_profile_plan_active ON profile_plan (profile_id) WHERE active;
CREATE INDEX idx_study_plan_day_plan_day ON study_plan_day (study_plan_id, day_number);
CREATE INDEX idx_study_plan_item_day_sort ON study_plan_item (study_plan_day_id, sort_order);
CREATE INDEX idx_profile_plan_active ON profile_plan (profile_id, active);
