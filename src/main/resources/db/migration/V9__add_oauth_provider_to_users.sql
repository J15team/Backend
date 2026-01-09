-- V9__add_oauth_provider_to_users.sql: OAuth認証プロバイダー情報を追加

-- OAuth認証プロバイダー（google, github等）
ALTER TABLE users ADD COLUMN oauth_provider VARCHAR(50) NULL;

-- OAuth認証時のプロバイダー側ユーザーID
ALTER TABLE users ADD COLUMN oauth_provider_id VARCHAR(255) NULL;

-- OAuthユーザーの場合、パスワードは不要なのでNULLを許容するように変更
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;

-- OAuthプロバイダーとプロバイダーIDの組み合わせでの一意制約
-- （同じGoogleアカウントで複数登録を防止）
CREATE UNIQUE INDEX idx_users_oauth_provider_id 
    ON users(oauth_provider, oauth_provider_id) 
    WHERE oauth_provider IS NOT NULL AND oauth_provider_id IS NOT NULL;

-- 検索最適化用インデックス
CREATE INDEX idx_users_oauth_provider ON users(oauth_provider) WHERE oauth_provider IS NOT NULL;
