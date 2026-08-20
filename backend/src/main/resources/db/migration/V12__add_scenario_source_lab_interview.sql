CREATE TABLE IF NOT EXISTS scenario (
    id BIGSERIAL PRIMARY KEY,
    external_key VARCHAR(120) NOT NULL UNIQUE,
    title VARCHAR(300) NOT NULL,
    summary TEXT NOT NULL,
    star_level SMALLINT NOT NULL CHECK (star_level BETWEEN 1 AND 5),
    difficulty VARCHAR(32) NOT NULL DEFAULT 'HARD',
    origin_type VARCHAR(32) NOT NULL DEFAULT 'IMPORTED',
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    seed_pack VARCHAR(120),
    source_version VARCHAR(120),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE scenario ADD COLUMN IF NOT EXISTS seed_pack VARCHAR(120);
ALTER TABLE scenario ADD COLUMN IF NOT EXISTS source_version VARCHAR(120);
ALTER TABLE scenario ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
UPDATE scenario SET external_key = CONCAT('legacy-scenario-', id) WHERE external_key IS NULL;
UPDATE scenario SET summary = '' WHERE summary IS NULL;
ALTER TABLE scenario ALTER COLUMN external_key SET NOT NULL;
ALTER TABLE scenario ALTER COLUMN summary SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_scenario_external_key ON scenario (external_key);

CREATE TABLE IF NOT EXISTS scenario_case (
    id BIGSERIAL PRIMARY KEY,
    scenario_id BIGINT NOT NULL REFERENCES scenario(id) ON DELETE CASCADE,
    code VARCHAR(64) NOT NULL,
    title VARCHAR(300) NOT NULL,
    root_cause TEXT NOT NULL,
    prompt TEXT,
    expected_analysis JSONB NOT NULL DEFAULT '[]'::jsonb,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (scenario_id, code),
    UNIQUE (id, scenario_id)
);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'scenario_case' AND column_name = 'case_code'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'scenario_case' AND column_name = 'code'
    ) THEN
        ALTER TABLE scenario_case RENAME COLUMN case_code TO code;
    END IF;
END $$;

ALTER TABLE scenario_case ADD COLUMN IF NOT EXISTS prompt TEXT;
ALTER TABLE scenario_case ADD COLUMN IF NOT EXISTS expected_analysis JSONB NOT NULL DEFAULT '[]'::jsonb;
UPDATE scenario_case SET root_cause = '' WHERE root_cause IS NULL;
ALTER TABLE scenario_case ALTER COLUMN root_cause SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_scenario_case_scenario_code ON scenario_case (scenario_id, code);
CREATE UNIQUE INDEX IF NOT EXISTS uk_scenario_case_id_scenario ON scenario_case (id, scenario_id);

-- 12 种解决方案是跨场景一致的稳定词典，避免按场景复制 144 条同义记录。
CREATE TABLE IF NOT EXISTS scenario_solution (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    principle TEXT NOT NULL,
    pros TEXT,
    cons TEXT,
    boundary_text TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE scenario_solution ADD COLUMN IF NOT EXISTS pros TEXT;
ALTER TABLE scenario_solution ADD COLUMN IF NOT EXISTS cons TEXT;
ALTER TABLE scenario_solution ADD COLUMN IF NOT EXISTS boundary_text TEXT;
ALTER TABLE scenario_solution ADD COLUMN IF NOT EXISTS sort_order INTEGER NOT NULL DEFAULT 0;
UPDATE scenario_solution SET code = CONCAT('legacy-solution-', id) WHERE code IS NULL;
UPDATE scenario_solution SET principle = '' WHERE principle IS NULL;
ALTER TABLE scenario_solution ALTER COLUMN code SET NOT NULL;
ALTER TABLE scenario_solution ALTER COLUMN principle SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_scenario_solution_code ON scenario_solution (code);

CREATE TABLE IF NOT EXISTS scenario_case_solution (
    case_id BIGINT NOT NULL REFERENCES scenario_case(id) ON DELETE CASCADE,
    solution_id BIGINT NOT NULL REFERENCES scenario_solution(id) ON DELETE CASCADE,
    recommendation_level SMALLINT NOT NULL DEFAULT 3 CHECK (recommendation_level BETWEEN 1 AND 5),
    recommendation VARCHAR(32) NOT NULL DEFAULT 'CANDIDATE',
    reason TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (case_id, solution_id)
);

ALTER TABLE scenario_case_solution
    ADD COLUMN IF NOT EXISTS recommendation VARCHAR(32) NOT NULL DEFAULT 'CANDIDATE';
ALTER TABLE scenario_case_solution
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE TABLE scenario_attempt (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES profile(id),
    scenario_id BIGINT NOT NULL REFERENCES scenario(id),
    case_id BIGINT,
    client_attempt_id UUID NOT NULL,
    answer_text TEXT NOT NULL,
    self_rating SMALLINT CHECK (self_rating BETWEEN 1 AND 5),
    result_type VARCHAR(32) NOT NULL,
    duration_seconds INTEGER CHECK (duration_seconds IS NULL OR duration_seconds >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (profile_id, client_attempt_id),
    FOREIGN KEY (case_id, scenario_id) REFERENCES scenario_case(id, scenario_id)
);

CREATE TABLE scenario_seed_history (
    id BIGSERIAL PRIMARY KEY,
    seed_pack VARCHAR(120) NOT NULL,
    version VARCHAR(64) NOT NULL,
    checksum_sha256 CHAR(64) NOT NULL,
    scenario_count INTEGER NOT NULL,
    case_count INTEGER NOT NULL,
    solution_count INTEGER NOT NULL,
    imported_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (seed_pack, version)
);

CREATE INDEX idx_scenario_status_updated ON scenario (status, updated_at DESC, id DESC);
CREATE INDEX idx_scenario_attempt_profile_time ON scenario_attempt (profile_id, created_at DESC, id DESC);
CREATE INDEX idx_scenario_case_scenario_sort ON scenario_case (scenario_id, sort_order, id);
CREATE INDEX idx_scenario_solution_sort ON scenario_solution (sort_order, id);

CREATE TABLE source_snippet (
    id BIGSERIAL PRIMARY KEY,
    external_key VARCHAR(120) NOT NULL UNIQUE,
    topic_id BIGINT REFERENCES topic(id),
    language VARCHAR(32) NOT NULL,
    library_name VARCHAR(120) NOT NULL,
    version_label VARCHAR(120) NOT NULL,
    source_path VARCHAR(500),
    title VARCHAR(300) NOT NULL,
    summary TEXT NOT NULL,
    code_text TEXT NOT NULL,
    start_line INTEGER NOT NULL DEFAULT 1 CHECK (start_line >= 1),
    end_line INTEGER NOT NULL CHECK (end_line >= start_line),
    license_name VARCHAR(120),
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE source_annotation (
    id BIGSERIAL PRIMARY KEY,
    source_snippet_id BIGINT NOT NULL REFERENCES source_snippet(id) ON DELETE CASCADE,
    line_start INTEGER NOT NULL CHECK (line_start >= 1),
    line_end INTEGER NOT NULL CHECK (line_end >= line_start),
    title VARCHAR(200) NOT NULL,
    explanation TEXT NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_source_snippet_topic_version
    ON source_snippet (topic_id, version_label, updated_at DESC, id DESC)
    WHERE status = 'ENABLED';
CREATE INDEX idx_source_annotation_snippet_sort
    ON source_annotation (source_snippet_id, sort_order, id);

CREATE TABLE lab_definition (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    algorithm VARCHAR(120) NOT NULL,
    version_label VARCHAR(120) NOT NULL,
    initial_dataset JSONB NOT NULL DEFAULT '{}'::jsonb,
    config_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_lab_definition_status_sort ON lab_definition (status, sort_order, id);

CREATE TABLE interview_session (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES profile(id),
    mode VARCHAR(32) NOT NULL,
    topic_code VARCHAR(120),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    total_score NUMERIC(5, 2),
    started_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE interview_turn (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES interview_session(id) ON DELETE CASCADE,
    client_turn_id UUID NOT NULL,
    sequence_no INTEGER NOT NULL CHECK (sequence_no >= 1),
    prompt TEXT NOT NULL,
    answer_text TEXT NOT NULL,
    score NUMERIC(5, 2),
    feedback TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (session_id, client_turn_id),
    UNIQUE (session_id, sequence_no)
);

CREATE INDEX idx_interview_session_profile_time
    ON interview_session (profile_id, started_at DESC, id DESC);

-- 教学片段均为本项目自写伪代码，只保留说明算法边界所需的短内容。
INSERT INTO source_snippet (
    external_key, language, library_name, version_label, source_path,
    title, summary, code_text, start_line, end_line, license_name
) VALUES
    ('source-hashmap-bucket', 'JAVA', 'JavaInterviewLab', 'V0.3', 'teaching/HashBucket.java',
     'HashMap 桶定位教学伪代码', '展示哈希扰动、数组下标和碰撞处理的最小主线。',
     E'int hash = spread(key.hashCode());\nint index = (table.length - 1) & hash;\nNode head = table[index];\nreturn findByKey(head, key, hash);',
     1, 4, 'PROJECT_ORIGINAL'),
    ('source-aqs-acquire', 'JAVA', 'JavaInterviewLab', 'V0.3', 'teaching/AqsAcquire.java',
     'AQS 获取同步状态教学伪代码', '强调快速尝试失败后入队，而不是复制 JDK 实现。',
     E'if (tryAcquire(arg)) {\n    return ACQUIRED;\n}\nNode node = enqueueCurrentThread();\nreturn parkAndRetry(node, arg);',
     1, 5, 'PROJECT_ORIGINAL'),
    ('source-transaction-boundary', 'JAVA', 'JavaInterviewLab', 'V0.3', 'teaching/TransactionBoundary.java',
     '事务边界教学伪代码', '展示业务写入与可靠事件记录必须共享提交边界。',
     E'beginTransaction();\nupdateBusinessState(command);\nappendOutboxEvent(command.event());\ncommitTransaction();',
     1, 4, 'PROJECT_ORIGINAL')
ON CONFLICT (external_key) DO NOTHING;

INSERT INTO source_annotation (source_snippet_id, line_start, line_end, title, explanation, sort_order)
SELECT id, 1, 2, '定位不是排序', '位运算只决定桶位置；键相等仍需比较 hash 与 equals。', 0
FROM source_snippet WHERE external_key = 'source-hashmap-bucket'
ON CONFLICT DO NOTHING;

INSERT INTO source_annotation (source_snippet_id, line_start, line_end, title, explanation, sort_order)
SELECT id, 1, 4, '提交边界', '业务状态与 outbox 任一写入失败都应回滚，避免双写不一致。', 0
FROM source_snippet WHERE external_key = 'source-transaction-boundary'
ON CONFLICT DO NOTHING;

INSERT INTO lab_definition (
    code, title, description, algorithm, version_label, initial_dataset, config_json, sort_order
) VALUES
    ('hashmap-resize', 'HashMap 扩容实验', '观察容量翻倍后节点保留原索引或移动旧容量偏移。',
     'HASHMAP_RESIZE', 'OpenJDK 8-21 共同主线', '{"capacity":8,"hashes":[1,5,9,13]}'::jsonb,
     '{"maxSteps":20,"showBinary":true}'::jsonb, 10),
    ('thread-pool-queue', '线程池提交实验', '观察核心线程、队列、最大线程和拒绝策略的状态转换。',
     'THREAD_POOL_SUBMIT', 'Java 21', '{"core":2,"max":4,"queueCapacity":3,"tasks":8}'::jsonb,
     '{"maxSteps":30}'::jsonb, 20),
    ('bplus-tree-search', 'B+ 树查找实验', '观察从根节点到叶子节点的区间选择和顺序扫描。',
     'BPLUS_TREE_SEARCH', 'PostgreSQL 16 教学抽象', '{"order":4,"keys":[3,8,12,17,21,30]}'::jsonb,
     '{"target":17,"maxSteps":20}'::jsonb, 30)
ON CONFLICT (code) DO NOTHING;
