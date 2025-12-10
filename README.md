# J15 Backend

Spring Boot 3.2 + Kotlin によるバックエンドアプリケーション。オニオンアーキテクチャを採用しています。

## アーキテクチャ

このプロジェクトは**オニオンアーキテクチャ**を採用しています。

### レイヤー構成（内側から外側へ）

1. **Domain（ドメイン層）** - エンティティ、値オブジェクト、ドメインサービス
2. **Application（アプリケーション層）** - ユースケース、アプリケーションサービス
3. **Infrastructure（インフラストラクチャ層）** - リポジトリ実装、外部サービス連携
4. **Presentation（プレゼンテーション層）** - Controller、API エンドポイント

### 依存関係のルール

- 依存は常に**外側から内側**へ向かう
- 内側のレイヤーは外側のレイヤーを参照してはならない
- インターフェースはドメイン層で定義し、実装はインフラ層で行う

## 技術スタック

- **言語**: Kotlin 1.9.21
- **フレームワーク**: Spring Boot 3.2.0
- **JDK**: Java 17
- **データベース**: PostgreSQL 16
- **ビルドツール**: Gradle 8.5
- **マイグレーション**: Flyway
- **セキュリティ**: Spring Security
- **コンテナ**: Docker, Docker Compose

## セットアップ

### 前提条件

- Docker & Docker Compose
- Java 17 以上（ローカルビルド時）

### 環境構築

1. リポジトリをクローン

```bash
git clone https://github.com/J15team/Backend.git
cd backend
```

1. Docker Composeで起動

```bash
docker-compose up -d
```

アプリケーションは `http://localhost:8080` で起動します。

1. 動作確認

```bash
curl http://localhost:8080/api/health
```

### ローカルビルド

```bash
./gradlew build
```

### テスト

テストはDocker Compose環境でのE2Eテストのみ実行されます。

```bash
# アプリケーションを起動
docker-compose up -d

# APIの動作確認
curl http://localhost:8080/api/health
curl http://localhost:8080/api/sections
```

CI/CDではGitHub Actionsで自動的にE2Eテストが実行されます。

## API エンドポイント

詳細なAPI仕様については [API仕様書](./docs/API.md) を参照してください。

### 概要

- **認証API**: サインアップ、サインイン
- **セクションAPI**: セクション一覧取得、セクション詳細取得
- **進捗管理API**: 進捗状態取得、セクション完了マーク、完了状態チェック、完了削除
- **ヘルスチェックAPI**: アプリケーション稼働状態確認

### 主要エンドポイント一覧

| カテゴリ | メソッド | エンドポイント | 説明 |
|---------|---------|---------------|------|
| 認証 | POST | `/api/auth/signup` | 新規ユーザー登録 |
| 認証 | POST | `/api/auth/signin` | ログイン |
| セクション | GET | `/api/sections` | 全セクション一覧 |
| セクション | GET | `/api/sections/{sectionId}` | セクション詳細 |
| 進捗 | GET | `/api/progress/{userId}` | ユーザー進捗取得 |
| 進捗 | POST | `/api/progress/{userId}/sections` | セクション完了マーク |
| 進捗 | GET | `/api/progress/{userId}/sections/{sectionId}` | 完了状態チェック |
| 進捗 | DELETE | `/api/progress/{userId}/sections/{sectionId}` | 完了削除 |
| ヘルスチェック | GET | `/api/health` | 稼働状態確認 |

### クイックスタート例

#### サインアップ

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

#### サインイン

```bash
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

#### セクション一覧取得

```bash
curl http://localhost:8080/api/sections
```

#### 進捗状態取得

```bash
curl http://localhost:8080/api/progress/{userId}
詳細なリクエスト/レスポンス形式、エラーハンドリング、バリデーションルールについては [API仕様書](./docs/API.md) を参照してください。

## データベース設計

### マイグレーション

Flywayを使用してデータベースマイグレーションを管理しています。

マイグレーションファイルは `src/main/resources/db/migration/` に配置されています。

- `V1__create_users_table.sql` - ユーザーテーブルの作成
- `V2__create_sections_and_progress_tables.sql` - セクションと進捗管理テーブルの作成

### ER図

```text
users
├── user_id (UUID, PK)
├── username (VARCHAR, UNIQUE)
├── email (VARCHAR, UNIQUE)
├── password_hash (VARCHAR)
└── created_at (TIMESTAMP)

sections
├── section_id (INTEGER, PK) ← 0~100の進捗ステップ
├── title (VARCHAR)
└── description (TEXT)

user_cleared_sections
├── user_cleared_section_id (SERIAL, PK)
├── user_id (UUID, FK → users)
├── section_id (INTEGER, FK → sections)
├── completed_at (TIMESTAMP)
└── UNIQUE(user_id, section_id) ← 重複防止
```

## 開発ガイドライン

### コーディング規約

- すべてのコード、コメント、ドキュメントは**日本語**で記述
- オニオンアーキテクチャの原則を厳守
- 依存関係の方向が正しいか常に確認

### ブランチ戦略

- `main` - プロダクション環境
- `develop` - 開発環境（今後導入予定）
- `feature/*` - 機能開発ブランチ

### CI/CD

GitHub Actionsを使用してCI/CDパイプラインを構築しています。

- **トリガー**: `main`, `develop` へのプッシュ、全てのPull Request
- **テスト**: Docker Composeを使用したE2Eテスト
  - サインアップ・サインイン機能
  - バリデーション・エラーハンドリング
  - セクション一覧取得
  - 進捗管理フロー（完了マーク・進捗取得・重複チェック）
- **除外**: `*.md`, `docs/**` のみの変更時はCIをスキップ

ワークフローファイル: `.github/workflows/ci.yml`

### Pull Request レビュー

- レビューコメントは日本語で記述
- 指摘事項は具体的かつ建設的に
- 改善提案がある場合はコード例も含める
- 良い点があれば積極的に褒める
- オニオンアーキテクチャの原則に沿っているか確認

## ディレクトリ構造

```text
src/
├── main/
│   ├── kotlin/
│   │   └── com/j15/backend/
│   │       ├── BackendApplication.kt
│   │       ├── domain/              # ドメイン層
│   │       │   ├── model/
│   │       │   │   ├── user/        # ユーザー関連モデル
│   │       │   │   ├── section/     # セクション関連モデル
│   │       │   │   └── progress/    # 進捗管理モデル
│   │       │   ├── repository/
│   │       │   └── service/
│   │       ├── application/         # アプリケーション層
│   │       │   └── usecase/
│   │       ├── infrastructure/      # インフラ層
│   │       │   ├── config/
│   │       │   ├── persistence/
│   │       │   │   ├── entity/
│   │       │   │   ├── jpa/
│   │       │   │   ├── repository/
│   │       │   │   └── converter/
│   │       │   └── service/
│   │       └── presentation/        # プレゼンテーション層
│   │           ├── controller/
│   │           ├── dto/
│   │           │   ├── request/
│   │           │   └── response/
│   │           └── exception/
│   └── resources/
│       ├── application.yml
│       └── db/migration/
└── .github/
    └── workflows/
        └── ci.yml                   # E2Eテスト定義
```

## 将来実装予定の機能

### セクション動的管理API

現在、セクションはデータベースマイグレーションによる静的管理のみ対応していますが、将来的に以下の動的管理APIの実装を予定しています。

**予定機能:**

- セクション新規登録 (`POST /api/sections`)
- セクション更新 (`PUT /api/sections/{sectionId}`)
- セクション削除 (`DELETE /api/sections/{sectionId}`)

**実装時の考慮事項:**

- 管理者権限の実装
- 既存進捗データとの整合性保持
- セクション順序管理
- 監査ログ機能

詳細は [API仕様書 - 将来実装予定](./docs/API.md#将来実装予定-セクション管理api) を参照してください。

## トラブルシューティング

### Dockerコンテナが起動しない

```bash
docker-compose down -v
docker-compose up -d
```

### データベース接続エラー

`docker-compose.yml` の環境変数と `application.yml` の設定が一致しているか確認してください。

### ポートが既に使用されている

8080ポートが使用中の場合は、`docker-compose.yml` のポート設定を変更してください。

## ライセンス

TBD
