-- セクションテーブルの作成
CREATE TABLE sections (
    section_id INTEGER PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT
);

-- ユーザー完了記録テーブルの作成
CREATE TABLE user_cleared_sections (
    user_cleared_section_id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    section_id INTEGER NOT NULL,
    completed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_section FOREIGN KEY (section_id) REFERENCES sections(section_id) ON DELETE CASCADE,
    CONSTRAINT uk_user_section UNIQUE (user_id, section_id)
);

-- インデックスの作成（パフォーマンス最適化）
CREATE INDEX idx_user_cleared_sections_user_id ON user_cleared_sections(user_id);
CREATE INDEX idx_user_cleared_sections_section_id ON user_cleared_sections(section_id);

-- 初期セクションデータの投入（0~100の進捗ステップ）
INSERT INTO sections (section_id, title, description) VALUES
(0, 'プロジェクト準備', 'アプリ開発の準備段階'),
(1, '環境構築', '開発環境のセットアップ'),
(2, '基本設定', 'プロジェクトの基本設定'),
(3, 'UI設計', 'ユーザーインターフェースの設計'),
(4, 'データベース設計', 'データモデルの設計'),
(5, '認証機能実装', 'ログイン・サインアップの実装'),
(10, 'フロントエンド開発開始', 'フロントエンドの基礎実装'),
(20, 'バックエンド開発開始', 'サーバーサイドの基礎実装'),
(30, 'API実装', 'RESTful APIの実装'),
(40, '機能実装50%', '主要機能の半分を実装'),
(50, '機能実装完了', '全機能の実装完了'),
(60, 'テスト作成', 'ユニットテストの作成'),
(70, 'デバッグ', 'バグの修正と調整'),
(80, 'UI/UX改善', 'ユーザー体験の最適化'),
(90, 'デプロイ準備', '本番環境への準備'),
(100, 'リリース完了', 'アプリの公開完了');
