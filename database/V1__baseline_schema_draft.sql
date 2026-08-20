-- Java Interview Lab 数据库基线草案
-- 注意：这是设计稿。真正开发 A04 时由 Agent 根据后端实体和约束复核后生成正式 Flyway Migration。
-- 所有注释使用简体中文，文件编码必须为 UTF-8。

CREATE TABLE IF NOT EXISTS profile (
    id              BIGSERIAL PRIMARY KEY,
    display_name    VARCHAR(100) NOT NULL,
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS category (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(64) NOT NULL UNIQUE,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    status          VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS topic (
    id              BIGSERIAL PRIMARY KEY,
    category_id     BIGINT NOT NULL REFERENCES category(id),
    code            VARCHAR(64) NOT NULL UNIQUE,
    name            VARCHAR(160) NOT NULL,
    description     TEXT,
    star_level      SMALLINT NOT NULL DEFAULT 3 CHECK (star_level BETWEEN 1 AND 5),
    sort_order      INTEGER NOT NULL DEFAULT 0,
    status          VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS question (
    id                  BIGSERIAL PRIMARY KEY,
    topic_id            BIGINT NOT NULL REFERENCES topic(id),
    external_key        VARCHAR(120),
    title               TEXT NOT NULL,
    question_type       VARCHAR(32) NOT NULL,
    star_level          SMALLINT NOT NULL CHECK (star_level BETWEEN 1 AND 5),
    difficulty          VARCHAR(32) NOT NULL,
    frequency_level     VARCHAR(32) NOT NULL,
    origin_type         VARCHAR(32) NOT NULL DEFAULT 'BUILTIN',
    status              VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    one_liner           TEXT,
    plain_explanation   TEXT,
    design_reason       TEXT,
    common_mistakes     TEXT,
    score_points        TEXT,
    extra_json          JSONB NOT NULL DEFAULT '{}'::jsonb,
    version             BIGINT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (external_key)
);

CREATE TABLE IF NOT EXISTS question_answer (
    id              BIGSERIAL PRIMARY KEY,
    question_id     BIGINT NOT NULL REFERENCES question(id) ON DELETE CASCADE,
    answer_type     VARCHAR(32) NOT NULL,
    content         TEXT NOT NULL,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(question_id, answer_type)
);

CREATE TABLE IF NOT EXISTS question_follow_up (
    id                  BIGSERIAL PRIMARY KEY,
    question_id         BIGINT NOT NULL REFERENCES question(id) ON DELETE CASCADE,
    parent_follow_up_id BIGINT REFERENCES question_follow_up(id),
    title               TEXT NOT NULL,
    reference_answer    TEXT,
    sort_order          INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tag (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(64) NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS question_tag (
    question_id BIGINT NOT NULL REFERENCES question(id) ON DELETE CASCADE,
    tag_id      BIGINT NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY(question_id, tag_id)
);

CREATE TABLE IF NOT EXISTS content_source (
    id              BIGSERIAL PRIMARY KEY,
    source_type     VARCHAR(32) NOT NULL,
    title           VARCHAR(300),
    url             TEXT,
    license_name    VARCHAR(120),
    retrieved_at    TIMESTAMPTZ,
    note            TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS content_relation (
    id              BIGSERIAL PRIMARY KEY,
    source_type     VARCHAR(32) NOT NULL,
    source_id       BIGINT NOT NULL,
    relation_type   VARCHAR(32) NOT NULL,
    target_type     VARCHAR(32) NOT NULL,
    target_id       BIGINT NOT NULL,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- V0.2：学习进度
CREATE TABLE IF NOT EXISTS study_progress (
    id                  BIGSERIAL PRIMARY KEY,
    profile_id          BIGINT NOT NULL REFERENCES profile(id),
    question_id         BIGINT NOT NULL REFERENCES question(id),
    stage               VARCHAR(32) NOT NULL DEFAULT 'PREVIEW',
    mastery_level       VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN',
    attempt_count       INTEGER NOT NULL DEFAULT 0,
    wrong_count         INTEGER NOT NULL DEFAULT 0,
    last_studied_at     TIMESTAMPTZ,
    next_review_at      TIMESTAMPTZ,
    version             BIGINT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(profile_id, question_id)
);

CREATE TABLE IF NOT EXISTS question_attempt (
    id                  BIGSERIAL PRIMARY KEY,
    profile_id          BIGINT NOT NULL REFERENCES profile(id),
    question_id         BIGINT NOT NULL REFERENCES question(id),
    answer_text         TEXT,
    self_rating         SMALLINT CHECK (self_rating BETWEEN 1 AND 5),
    viewed_answer       BOOLEAN NOT NULL DEFAULT FALSE,
    marked_wrong        BOOLEAN NOT NULL DEFAULT FALSE,
    elapsed_ms          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS favorite (
    id              BIGSERIAL PRIMARY KEY,
    profile_id      BIGINT NOT NULL REFERENCES profile(id),
    target_type     VARCHAR(32) NOT NULL,
    target_id       BIGINT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(profile_id, target_type, target_id)
);

CREATE TABLE IF NOT EXISTS note (
    id              BIGSERIAL PRIMARY KEY,
    profile_id      BIGINT NOT NULL REFERENCES profile(id),
    target_type     VARCHAR(32) NOT NULL,
    target_id       BIGINT NOT NULL,
    content         TEXT NOT NULL DEFAULT '',
    version         BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(profile_id, target_type, target_id)
);

CREATE TABLE IF NOT EXISTS review_task (
    id              BIGSERIAL PRIMARY KEY,
    profile_id      BIGINT NOT NULL REFERENCES profile(id),
    question_id     BIGINT NOT NULL REFERENCES question(id),
    due_at          TIMESTAMPTZ NOT NULL,
    status          VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- V0.3：场景
CREATE TABLE IF NOT EXISTS scenario (
    id                  BIGSERIAL PRIMARY KEY,
    external_key        VARCHAR(120) UNIQUE,
    title               VARCHAR(300) NOT NULL,
    summary             TEXT,
    star_level          SMALLINT NOT NULL CHECK (star_level BETWEEN 1 AND 5),
    difficulty          VARCHAR(32) NOT NULL,
    origin_type         VARCHAR(32) NOT NULL DEFAULT 'BUILTIN',
    status              VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS scenario_case (
    id                  BIGSERIAL PRIMARY KEY,
    scenario_id         BIGINT NOT NULL REFERENCES scenario(id) ON DELETE CASCADE,
    case_code           VARCHAR(32) NOT NULL,
    title               VARCHAR(300) NOT NULL,
    description         TEXT,
    root_cause          TEXT,
    risks               TEXT,
    wrong_approach      TEXT,
    sort_order          INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(scenario_id, case_code)
);

CREATE TABLE IF NOT EXISTS scenario_solution (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(120) UNIQUE,
    name                VARCHAR(200) NOT NULL,
    principle           TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS scenario_case_solution (
    case_id                 BIGINT NOT NULL REFERENCES scenario_case(id) ON DELETE CASCADE,
    solution_id             BIGINT NOT NULL REFERENCES scenario_solution(id),
    recommendation_level    SMALLINT NOT NULL DEFAULT 3 CHECK (recommendation_level BETWEEN 1 AND 5),
    is_final_defense        BOOLEAN NOT NULL DEFAULT FALSE,
    applicability          TEXT,
    pros                   TEXT,
    cons                   TEXT,
    risks                  TEXT,
    reason                 TEXT,
    sort_order             INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY(case_id, solution_id)
);
