-- V2__add_role_to_users.sql: ユーザーテーブルにロール列を追加

ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER';

-- 既存インデックスの追加
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);
