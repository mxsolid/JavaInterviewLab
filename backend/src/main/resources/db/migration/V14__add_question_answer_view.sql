-- 答案披露是独立学习事实，只追加，不与最终答题 attempt 混写。
CREATE TABLE question_answer_view (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES profile(id),
    question_id BIGINT NOT NULL REFERENCES question(id),
    client_view_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (profile_id, client_view_id)
);

CREATE INDEX idx_question_answer_view_profile_question_time
    ON question_answer_view (profile_id, question_id, created_at DESC, id DESC);
