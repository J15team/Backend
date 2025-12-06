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

2. Docker Composeで起動

```bash
docker-compose up -d
```

アプリケーションは `http://localhost:8080` で起動します。

3. 動作確認

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

### ヘルスチェック

```
GET /api/health
```

### 認証

#### サインアップ

```
POST /api/users/signup
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}
```

#### サインイン

```
POST /api/auth/signin
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}
```

レスポンス例:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "username": "testuser",
  "email": "test@example.com"
}
```

### Actuator

```
GET /actuator/health
GET /actuator/info
```

### 進捗管理

#### 全セクション一覧取得

```
GET /api/sections
```

レスポンス例:

```json
[
  {
    "sectionId": 0,
    "title": "プロジェクト準備",
    "description": "アプリ開発の準備段階"
  },
  {
    "sectionId": 1,
    "title": "環境構築",
    "description": "開発環境のセットアップ"
  }
]
```

#### ユーザー進捗状態取得

```
GET /api/progress/{userId}
```

レスポンス例:

```json
{
  "userId": "a4153a84-6ab1-45f2-a7ee-8522e3f050ed",
  "progressPercentage": 1.98,
  "clearedCount": 2,
  "remainingCount": 99,
  "totalSections": 101,
  "isAllCleared": false,
  "nextSectionId": 2,
  "clearedSections": [
    {
      "sectionId": 0,
      "completedAt": "2025-12-06T11:06:06.412403Z"
    }
  ]
}
```

#### セクション完了マーク

```
POST /api/progress/{userId}/sections
Content-Type: application/json

{
  "sectionId": 0
}
```

#### セクション完了状態チェック

```
GET /api/progress/{userId}/sections/{sectionId}
```

#### セクション完了削除（デバッグ用）

```
DELETE /api/progress/{userId}/sections/{sectionId}
```

## データベース

### マイグレーション

Flywayを使用してデータベースマイグレーションを管理しています。

マイグレーションファイルは `src/main/resources/db/migration/` に配置されています。

- `V1__create_users_table.sql` - ユーザーテーブルの作成
- `V2__create_sections_and_progress_tables.sql` - セクションと進捗管理テーブルの作成

### ER図

```
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

```
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
