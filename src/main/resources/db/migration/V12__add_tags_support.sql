-- V12: タグ機能の追加

-- タグテーブル（IDは自動生成）
CREATE TABLE tags (
    tag_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_tag_type CHECK (type IN ('NORMAL', 'PREMIUM'))
);

-- 題材-タグ関連テーブル（多対多）
CREATE TABLE subject_tags (
    subject_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (subject_id, tag_id),
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE
);

-- インデックス
CREATE INDEX idx_tags_name ON tags(name);
CREATE INDEX idx_tags_type ON tags(type);
CREATE INDEX idx_subject_tags_tag_id ON subject_tags(tag_id);
