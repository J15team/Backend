-- V3: 題材(Subject)機能の追加

-- 題材テーブル作成
CREATE TABLE subjects (
    subject_id BIGINT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    max_sections INTEGER NOT NULL CHECK (max_sections >= 1 AND max_sections <= 1000),
    created_at TIMESTAMP NOT NULL
);

-- 既存のsectionsテーブルにsubject_idカラムを追加
ALTER TABLE sections ADD COLUMN subject_id BIGINT NOT NULL DEFAULT 1;

-- sectionsテーブルの外部キー制約を追加
ALTER TABLE sections ADD CONSTRAINT fk_sections_subject 
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE;

-- sectionsテーブルの主キーを複合キーに変更
-- 1. 既存の主キー制約を削除
ALTER TABLE sections DROP CONSTRAINT sections_pkey;
-- 2. 新しい複合主キーを追加
ALTER TABLE sections ADD PRIMARY KEY (subject_id, section_id);

-- 既存のuser_cleared_sectionsテーブルにsubject_idカラムを追加
ALTER TABLE user_cleared_sections ADD COLUMN subject_id BIGINT NOT NULL DEFAULT 1;

-- user_cleared_sectionsテーブルの外部キー制約を追加
ALTER TABLE user_cleared_sections ADD CONSTRAINT fk_user_cleared_sections_subject 
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE;

-- user_cleared_sectionsテーブルのユニーク制約を更新
-- 1. 既存のユニーク制約を削除
ALTER TABLE user_cleared_sections DROP CONSTRAINT uk_user_section;
-- 2. 新しいユニーク制約を追加（user_id, subject_id, section_id）
ALTER TABLE user_cleared_sections ADD CONSTRAINT uk_user_subject_section 
    UNIQUE (user_id, subject_id, section_id);

-- デフォルトの題材を作成（既存データとの互換性のため）
INSERT INTO subjects (subject_id, title, description, max_sections, created_at)
VALUES (1, 'デフォルト題材', 'マイグレーションで作成されたデフォルト題材', 101, CURRENT_TIMESTAMP);

-- 既存のsectionsとuser_cleared_sectionsのsubject_idはすでにDEFAULT 1で設定されているため、追加の更新は不要

-- インデックス作成
CREATE INDEX idx_sections_subject_id ON sections(subject_id);
CREATE INDEX idx_user_cleared_sections_subject_id ON user_cleared_sections(subject_id);
CREATE INDEX idx_user_cleared_sections_user_subject ON user_cleared_sections(user_id, subject_id);
