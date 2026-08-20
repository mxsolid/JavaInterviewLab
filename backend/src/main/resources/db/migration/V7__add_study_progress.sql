CREATE TABLE IF NOT EXISTS study_progress (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES profile(id),
    question_id BIGINT NOT NULL REFERENCES question(id),
    stage VARCHAR(32) NOT NULL,
    mastery_level VARCHAR(32) NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    wrong_count INTEGER NOT NULL DEFAULT 0,
    wrong_book_active BOOLEAN NOT NULL DEFAULT FALSE,
    last_studied_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (profile_id, question_id)
);

ALTER TABLE study_progress ADD COLUMN IF NOT EXISTS wrong_book_active BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE study_progress SET last_studied_at = created_at WHERE last_studied_at IS NULL;
ALTER TABLE study_progress ALTER COLUMN last_studied_at SET NOT NULL;

CREATE INDEX idx_progress_profile_last_studied
ON study_progress (profile_id, last_studied_at DESC);
