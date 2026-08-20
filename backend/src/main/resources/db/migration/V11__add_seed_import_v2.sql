ALTER TABLE question
    ADD COLUMN seed_pack VARCHAR(120),
    ADD COLUMN source_version VARCHAR(300);

CREATE TABLE seed_import_history (
    id BIGSERIAL PRIMARY KEY,
    seed_pack VARCHAR(120) NOT NULL,
    version VARCHAR(64) NOT NULL,
    checksum_sha256 CHAR(64) NOT NULL,
    import_mode VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_count INTEGER NOT NULL DEFAULT 0,
    updated_count INTEGER NOT NULL DEFAULT 0,
    skipped_count INTEGER NOT NULL DEFAULT 0,
    error_count INTEGER NOT NULL DEFAULT 0,
    duration_ms BIGINT NOT NULL DEFAULT 0,
    imported_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (seed_pack, version)
);

-- 题目列表按专题、状态过滤后按更新时间倒序分页，该复合索引对应现有 QuestionMapper 查询。
CREATE INDEX idx_question_topic_status_updated
    ON question (topic_id, status, updated_at DESC, id DESC);

CREATE INDEX idx_seed_import_history_pack_time
    ON seed_import_history (seed_pack, imported_at DESC, id DESC);
