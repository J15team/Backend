-- V4__add_user_role_column.sql: ユーザーテーブルにロールカラムを追加

-- ロールカラムを追加（デフォルト値: ROLE_USER）
ALTER TABLE users
ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER';

-- ロールカラムにインデックスを作成（検索最適化）
CREATE INDEX idx_users_role ON users(role);

-- 既存データに対してもデフォルト値を設定
UPDATE users SET role = 'ROLE_USER' WHERE role IS NULL;
