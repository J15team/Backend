-- V6: 画像管理テーブルの作成（セクションとの1:n関係）

-- 画像テーブルの作成
CREATE TABLE images (
    image_id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT NOT NULL,
    section_id INTEGER NOT NULL,
    image_url TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 外部キー制約（ON DELETE CASCADEで親削除時に自動削除）
    CONSTRAINT fk_images_section
        FOREIGN KEY (subject_id, section_id)
        REFERENCES sections(subject_id, section_id)
        ON DELETE CASCADE
);

-- インデックス作成（検索パフォーマンス向上）
CREATE INDEX idx_images_subject_section ON images(subject_id, section_id);
CREATE INDEX idx_images_subject_id ON images(subject_id);
CREATE INDEX idx_images_created_at ON images(created_at);

-- 既存データの移行（sections.image_urlからimagesテーブルへ）
INSERT INTO images (subject_id, section_id, image_url, created_at)
SELECT subject_id, section_id, image_url, CURRENT_TIMESTAMP
FROM sections
WHERE image_url IS NOT NULL AND image_url != '';

-- コメント追加
COMMENT ON TABLE images IS 'セクションに関連付けられた画像を管理するテーブル（1:n関係）';
COMMENT ON COLUMN images.image_id IS '画像ID（自動生成）';
COMMENT ON COLUMN images.subject_id IS '題材ID';
COMMENT ON COLUMN images.section_id IS 'セクションID';
COMMENT ON COLUMN images.image_url IS '画像のS3 URL';
COMMENT ON COLUMN images.created_at IS '画像の登録日時';
