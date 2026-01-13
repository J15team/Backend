-- V13: ランキング機能の追加（題材・タグの閲覧記録）

-- 題材閲覧記録テーブル（ユーザーごとに1回のみカウント）
CREATE TABLE subject_views (
    subject_id BIGINT NOT NULL,
    user_id UUID NOT NULL,
    viewed_at TIMESTAMP NOT NULL,
    PRIMARY KEY (subject_id, user_id),
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- タグ閲覧記録テーブル（ユーザーごとに1回のみカウント）
CREATE TABLE tag_views (
    tag_id BIGINT NOT NULL,
    user_id UUID NOT NULL,
    viewed_at TIMESTAMP NOT NULL,
    PRIMARY KEY (tag_id, user_id),
    FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- インデックス（ランキング集計用）
CREATE INDEX idx_subject_views_subject_id ON subject_views(subject_id);
CREATE INDEX idx_tag_views_tag_id ON tag_views(tag_id);

-- ユーザー別検索用インデックス
CREATE INDEX idx_subject_views_user_id ON subject_views(user_id);
CREATE INDEX idx_tag_views_user_id ON tag_views(user_id);
