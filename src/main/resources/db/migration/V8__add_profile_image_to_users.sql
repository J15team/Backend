-- V8: ユーザーテーブルにプロフィール画像URLカラムを追加

-- プロフィール画像URLカラムを追加（nullable）
ALTER TABLE users ADD COLUMN profile_image_url VARCHAR(2048);

-- コメント追加
COMMENT ON COLUMN users.profile_image_url IS 'ユーザーのプロフィール画像URL（S3）';
