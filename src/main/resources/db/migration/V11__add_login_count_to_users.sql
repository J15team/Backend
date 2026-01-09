-- ユーザーテーブルにログインカウントと最終ログイン日時を追加
-- 用途: 初回ログイン検知（チュートリアル表示）、ログイン統計

-- ログイン回数カラムを追加（デフォルト0）
ALTER TABLE users ADD COLUMN login_count INTEGER NOT NULL DEFAULT 0;

-- 最終ログイン日時カラムを追加
ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP WITH TIME ZONE;
