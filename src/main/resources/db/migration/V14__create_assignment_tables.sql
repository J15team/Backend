-- V14: 課題実行システム用テーブル作成
-- 既存のsubjects/sectionsテーブルとは完全に分離

-- assignment_subjects テーブル（課題題材）
-- 既存のsubjectsテーブルと同じ仕様
CREATE TABLE assignment_subjects (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    max_sections INTEGER NOT NULL CHECK (max_sections >= 1 AND max_sections <= 1000),
    weight INTEGER NOT NULL DEFAULT 1 CHECK (weight >= 1 AND weight <= 5),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- assignment_sections テーブル（課題セクション）
-- 説明セクションと課題セクションを混在可能
CREATE TABLE assignment_sections (
    assignment_subject_id BIGINT NOT NULL REFERENCES assignment_subjects(id) ON DELETE CASCADE,
    section_id INTEGER NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    has_assignment BOOLEAN NOT NULL DEFAULT FALSE,
    test_cases JSONB,           -- テストケース（JSON形式）
    time_limit INTEGER,         -- 制限時間（ミリ秒）
    memory_limit INTEGER,       -- メモリ制限（MB）
    PRIMARY KEY (assignment_subject_id, section_id)
);

-- submissions テーブル（提出）
-- INSERT only（更新・削除禁止）
CREATE TABLE submissions (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    assignment_subject_id BIGINT NOT NULL,
    section_id INTEGER NOT NULL,
    code TEXT NOT NULL,
    language VARCHAR(20) NOT NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    score INTEGER,
    total_test_cases INTEGER,
    passed_test_cases INTEGER,
    FOREIGN KEY (assignment_subject_id, section_id) 
        REFERENCES assignment_sections(assignment_subject_id, section_id) ON DELETE CASCADE
);

-- INSERT only制約（トリガーで実装）
CREATE OR REPLACE FUNCTION prevent_submission_update()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Submissions cannot be updated or deleted';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER no_update_submissions
BEFORE UPDATE OR DELETE ON submissions
FOR EACH ROW EXECUTE FUNCTION prevent_submission_update();

-- test_results テーブル（テスト結果）
CREATE TABLE test_results (
    id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    test_case_index INTEGER NOT NULL,
    verdict VARCHAR(10) NOT NULL,
    execution_time INTEGER,     -- 実行時間（ミリ秒）
    memory_used INTEGER,        -- メモリ使用量（KB）
    visible BOOLEAN NOT NULL,
    actual_output TEXT,
    error_message TEXT
);

-- インデックス
CREATE INDEX idx_assignment_sections_subject ON assignment_sections(assignment_subject_id);
CREATE INDEX idx_submissions_user_section ON submissions(user_id, assignment_subject_id, section_id);
CREATE INDEX idx_submissions_status ON submissions(status);
CREATE INDEX idx_test_results_submission ON test_results(submission_id);
