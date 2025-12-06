-- V1__create_users_table.sql: ユーザーテーブルを作成する

-- UUID生成に必要
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    -- 主キー: UUID（アプリ側のVOに合わせる）
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- ユーザー名: NOT NULL (NULL不可), UNIQUE (一意)
    username VARCHAR(20) NOT NULL UNIQUE,

    -- メールアドレス: NOT NULL, UNIQUE (ログイン認証の識別子)
    email VARCHAR(255) NOT NULL UNIQUE,

    -- パスワードハッシュ: NOT NULL (ハッシュ化された文字列)
    password_hash VARCHAR(255) NOT NULL,

    -- 登録日時: TIMESTAMP WITH TIME ZONE (タイムゾーン情報付きタイムスタンプ)
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 検索最適化のためのインデックスを追加