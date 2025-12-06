# API 仕様書

## 目次

- [認証API](#認証api)
  - [サインアップ](#サインアップ)
  - [サインイン](#サインイン)
- [セクションAPI](#セクションapi)
  - [全セクション一覧取得](#全セクション一覧取得)
  - [セクション詳細取得](#セクション詳細取得)
  - [⚠️ 将来実装予定](#将来実装予定-セクション管理api)
- [進捗管理API](#進捗管理api)
  - [ユーザー進捗状態取得](#ユーザー進捗状態取得)
  - [セクション完了マーク](#セクション完了マーク)
  - [セクション完了状態チェック](#セクション完了状態チェック)
  - [セクション完了削除](#セクション完了削除-デバッグ用)
- [ヘルスチェックAPI](#ヘルスチェックapi)

---

## 認証API

### サインアップ

新規ユーザーを登録します。

**エンドポイント**

```
POST /api/users/signup
```

**認証**

不要

**リクエストヘッダー**

```
Content-Type: application/json
```

**リクエストボディ**

| フィールド | 型 | 必須 | 説明 | バリデーション |
|-----------|-----|------|------|---------------|
| username | string | ○ | ユーザー名 | 3~50文字、英数字とアンダースコアのみ |
| email | string | ○ | メールアドレス | メール形式、一意制約 |
| password | string | ○ | パスワード | 8文字以上 |

**リクエスト例**

```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}
```

**レスポンス**

**成功時 (201 Created)**

```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "username": "testuser",
  "email": "test@example.com",
  "createdAt": "2025-12-06T10:00:00Z"
}
```

**エラーレスポンス**

**400 Bad Request** - バリデーションエラー

```json
{
  "error": "Validation failed",
  "details": [
    "Username must be between 3 and 50 characters",
    "Password must be at least 8 characters"
  ]
}
```

**409 Conflict** - メールアドレスまたはユーザー名が既に存在

```json
{
  "error": "User already exists",
  "message": "Email or username is already registered"
}
```

---

### サインイン

既存ユーザーでログインし、JWTトークンを取得します。

**エンドポイント**

```
POST /api/auth/signin
```

**認証**

不要

**リクエストヘッダー**

```
Content-Type: application/json
```

**リクエストボディ**

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| email | string | ○ | メールアドレス |
| password | string | ○ | パスワード |

**リクエスト例**

```json
{
  "email": "test@example.com",
  "password": "password123"
}
```

**レスポンス**

**成功時 (200 OK)**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "username": "testuser",
  "email": "test@example.com"
}
```

**エラーレスポンス**

**401 Unauthorized** - 認証失敗

```json
{
  "error": "Authentication failed",
  "message": "Invalid email or password"
}
```

**400 Bad Request** - リクエスト不正

```json
{
  "error": "Invalid request",
  "message": "Email and password are required"
}
```

---

## セクションAPI

### 全セクション一覧取得

利用可能な全セクション（0~100）の一覧を取得します。

**エンドポイント**

```
GET /api/sections
```

**認証**

不要

**クエリパラメータ**

なし

**レスポンス**

**成功時 (200 OK)**

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
  },
  {
    "sectionId": 2,
    "title": "基本設計",
    "description": "アプリケーションの基本設計"
  }
]
```

**レスポンスフィールド**

| フィールド | 型 | 説明 |
|-----------|-----|------|
| sectionId | integer | セクションID (0~100) |
| title | string | セクションタイトル |
| description | string | セクション説明 |

**エラーレスポンス**

**500 Internal Server Error** - サーバーエラー

```json
{
  "error": "Internal server error",
  "message": "Failed to retrieve sections"
}
```

---

### 将来実装予定: セクション管理API

**⚠️ 重要: 以下のAPIは将来実装予定です（現在は未実装）**

セクションの動的管理機能として、以下のエンドポイントの実装を予定しています。

#### 予定されている機能

**1. セクション新規登録**
```
POST /api/sections
```
- 管理者のみ実行可能
- セクションタイトル、説明、順序などを指定して新規セクションを作成

**2. セクション更新**
```
PUT /api/sections/{sectionId}
```
- 管理者のみ実行可能
- 既存セクションのタイトル、説明、順序などを更新

**3. セクション削除**
```
DELETE /api/sections/{sectionId}
```
- 管理者のみ実行可能
- セクションを論理削除または物理削除
- 進捗データとの整合性を保つための検討が必要

#### 実装時の考慮事項

- **権限管理**: 管理者ロールの実装とアクセス制御
- **データ整合性**: 既存の進捗データとの関連性を保つ仕組み
- **セクション順序**: セクションの表示順序管理
- **バリデーション**: セクションID範囲（0~100）の制約見直し
- **監査ログ**: セクション変更履歴の記録

現在はデータベースマイグレーションによる静的管理のみ対応しています。

---

### セクション詳細取得

指定されたセクションIDの詳細情報を取得します。

**エンドポイント**

```
GET /api/sections/{sectionId}
```

**認証**

不要

**パスパラメータ**

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| sectionId | integer | ○ | セクションID (0~100) |

**リクエスト例**

```
GET /api/sections/5
```

**レスポンス**

**成功時 (200 OK)**

```json
{
  "sectionId": 5,
  "title": "データベース設計",
  "description": "データベーススキーマの設計と実装"
}
```

**エラーレスポンス**

**404 Not Found** - セクションが存在しない

```json
{
  "error": "Section not found",
  "message": "Section with ID 150 does not exist"
}
```

**400 Bad Request** - 無効なセクションID

```json
{
  "error": "Invalid section ID",
  "message": "Section ID must be between 0 and 100"
}
```

---

## 進捗管理API

### ユーザー進捗状態取得

指定されたユーザーの進捗状態を取得します。

**エンドポイント**

```
GET /api/progress/{userId}
```

**認証**

不要（将来的には認証必須になる可能性あり）

**パスパラメータ**

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| userId | UUID | ○ | ユーザーID |

**リクエスト例**

```
GET /api/progress/a4153a84-6ab1-45f2-a7ee-8522e3f050ed
```

**レスポンス**

**成功時 (200 OK)**

```json
{
  "userId": "a4153a84-6ab1-45f2-a7ee-8522e3f050ed",
  "progressPercentage": 1,
  "clearedCount": 2,
  "remainingCount": 99,
  "totalSections": 101,
  "isAllCleared": false,
  "nextSectionId": 2,
  "clearedSections": [
    {
      "sectionId": 0,
      "completedAt": "2025-12-06T11:06:06.412403Z"
    },
    {
      "sectionId": 1,
      "completedAt": "2025-12-06T11:10:22.123456Z"
    }
  ]
}
```

**レスポンスフィールド**

| フィールド | 型 | 説明 |
|-----------|-----|------|
| userId | UUID | ユーザーID |
| progressPercentage | integer | 進捗率 (0~100の整数、全完了時は必ず100) |
| clearedCount | integer | 完了済みセクション数 |
| remainingCount | integer | 未完了セクション数 |
| totalSections | integer | 総セクション数 (101) |
| isAllCleared | boolean | 全セクション完了フラグ |
| nextSectionId | integer \| null | 次に完了すべきセクションID (全完了時はnull) |
| clearedSections | array | 完了済みセクション情報の配列 |
| clearedSections[].sectionId | integer | 完了済みセクションID |
| clearedSections[].completedAt | string (ISO 8601) | 完了日時 |

**注意事項**

- `progressPercentage` は0~100の整数値です
- 全セクション完了時は必ず100%を返します
- 整数除算により小数点以下は切り捨てられます

**エラーレスポンス**

**404 Not Found** - ユーザーが存在しない

```json
{
  "error": "User not found",
  "message": "User with ID a4153a84-6ab1-45f2-a7ee-8522e3f050ed does not exist"
}
```

**400 Bad Request** - 無効なユーザーID形式

```json
{
  "error": "Invalid user ID",
  "message": "User ID must be a valid UUID"
}
```

---

### セクション完了マーク

指定されたセクションを完了状態にします。

**エンドポイント**

```
POST /api/progress/{userId}/sections
```

**認証**

不要（将来的には認証必須になる可能性あり）

**パスパラメータ**

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| userId | UUID | ○ | ユーザーID |

**リクエストヘッダー**

```
Content-Type: application/json
```

**リクエストボディ**

| フィールド | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| sectionId | integer | ○ | 完了するセクションID (0~100) |

**リクエスト例**

```json
{
  "sectionId": 5
}
```

**レスポンス**

**成功時 (200 OK)**

```json
{
  "userId": "a4153a84-6ab1-45f2-a7ee-8522e3f050ed",
  "sectionId": 5,
  "completedAt": "2025-12-06T12:30:45.678901Z",
  "message": "Section marked as completed"
}
```

**エラーレスポンス**

**409 Conflict** - 既に完了済み

```json
{
  "error": "Already completed",
  "message": "Section 5 is already marked as completed for this user"
}
```

**404 Not Found** - ユーザーまたはセクションが存在しない

```json
{
  "error": "Not found",
  "message": "User or section does not exist"
}
```

**400 Bad Request** - 無効なセクションID

```json
{
  "error": "Invalid section ID",
  "message": "Section ID must be between 0 and 100"
}
```

---

### セクション完了状態チェック

指定されたセクションが完了済みかどうかをチェックします。

**エンドポイント**

```
GET /api/progress/{userId}/sections/{sectionId}
```

**認証**

不要（将来的には認証必須になる可能性あり）

**パスパラメータ**

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| userId | UUID | ○ | ユーザーID |
| sectionId | integer | ○ | セクションID (0~100) |

**リクエスト例**

```
GET /api/progress/a4153a84-6ab1-45f2-a7ee-8522e3f050ed/sections/5
```

**レスポンス**

**成功時 (200 OK)** - 完了済み

```json
{
  "userId": "a4153a84-6ab1-45f2-a7ee-8522e3f050ed",
  "sectionId": 5,
  "isCleared": true,
  "completedAt": "2025-12-06T12:30:45.678901Z"
}
```

**成功時 (200 OK)** - 未完了

```json
{
  "userId": "a4153a84-6ab1-45f2-a7ee-8522e3f050ed",
  "sectionId": 5,
  "isCleared": false,
  "completedAt": null
}
```

**エラーレスポンス**

**404 Not Found** - ユーザーが存在しない

```json
{
  "error": "User not found",
  "message": "User with ID a4153a84-6ab1-45f2-a7ee-8522e3f050ed does not exist"
}
```

**400 Bad Request** - 無効なセクションID

```json
{
  "error": "Invalid section ID",
  "message": "Section ID must be between 0 and 100"
}
```

---

### セクション完了削除 (デバッグ用)

指定されたセクションの完了状態を削除します。

**⚠️ 注意: このエンドポイントは開発/デバッグ用です。本番環境では無効化することを推奨します。**

**エンドポイント**

```
DELETE /api/progress/{userId}/sections/{sectionId}
```

**認証**

不要（将来的には認証必須になる可能性あり）

**パスパラメータ**

| パラメータ | 型 | 必須 | 説明 |
|-----------|-----|------|------|
| userId | UUID | ○ | ユーザーID |
| sectionId | integer | ○ | セクションID (0~100) |

**リクエスト例**

```
DELETE /api/progress/a4153a84-6ab1-45f2-a7ee-8522e3f050ed/sections/5
```

**レスポンス**

**成功時 (200 OK)**

```json
{
  "userId": "a4153a84-6ab1-45f2-a7ee-8522e3f050ed",
  "sectionId": 5,
  "message": "Section completion deleted"
}
```

**エラーレスポンス**

**404 Not Found** - 完了記録が存在しない

```json
{
  "error": "Not found",
  "message": "Section 5 completion record not found for this user"
}
```

**400 Bad Request** - 無効なセクションID

```json
{
  "error": "Invalid section ID",
  "message": "Section ID must be between 0 and 100"
}
```

---

## ヘルスチェックAPI

### アプリケーションヘルスチェック

アプリケーションの稼働状態を確認します。

**エンドポイント**

```
GET /api/health
```

**認証**

不要

**レスポンス**

**成功時 (200 OK)**

```json
{
  "status": "UP",
  "timestamp": "2025-12-06T10:00:00Z"
}
```

---

### Actuator ヘルスチェック

Spring Boot Actuatorのヘルスチェックエンドポイントです。

**エンドポイント**

```
GET /actuator/health
```

**認証**

不要

**レスポンス**

**成功時 (200 OK)**

```json
{
  "status": "UP"
}
```

---

### Actuator 情報

アプリケーションの情報を取得します。

**エンドポイント**

```
GET /actuator/info
```

**認証**

不要

**レスポンス**

**成功時 (200 OK)**

```json
{
  "app": {
    "name": "J15 Backend",
    "version": "1.0.0"
  }
}
```

---

## エラーハンドリング

### 共通エラーレスポンス形式

すべてのエラーレスポンスは以下の形式に従います：

```json
{
  "error": "エラーの種類",
  "message": "エラーの詳細メッセージ",
  "timestamp": "2025-12-06T10:00:00Z",
  "path": "/api/path/to/endpoint"
}
```

### HTTPステータスコード

| コード | 説明 | 使用例 |
|--------|------|--------|
| 200 | OK | リクエスト成功 |
| 201 | Created | リソース作成成功 |
| 400 | Bad Request | バリデーションエラー、不正なリクエスト |
| 401 | Unauthorized | 認証エラー |
| 403 | Forbidden | 権限エラー |
| 404 | Not Found | リソースが見つからない |
| 409 | Conflict | リソースの競合（重複など） |
| 500 | Internal Server Error | サーバー内部エラー |

---

## データ型

### UUID形式

UUIDは以下の形式で表現されます：

```
123e4567-e89b-12d3-a456-426614174000
```

### ISO 8601日時形式

日時は以下のISO 8601形式で表現されます：

```
2025-12-06T10:00:00Z
```

または

```
2025-12-06T10:00:00.123456Z
```

---

## レート制限

現在、レート制限は実装されていません。将来的に実装される可能性があります。

---

## バージョニング

現在、APIバージョニングは実装されていません。APIのベースパスは `/api` です。

---

## セキュリティ

### 認証

現在、以下のエンドポイントは認証不要です：

- `/api/health`
- `/api/users/signup`
- `/api/auth/signin`
- `/api/sections/**`
- `/api/progress/**`
- `/actuator/**`

将来的にはJWTベースの認証が必須になる予定です。

### CORS

CORS設定については `SecurityConfig` を参照してください。

---

## 変更履歴

### v1.0.0 (2025-12-06)

- 初版リリース
- 認証API（サインアップ、サインイン）
- セクションAPI（一覧取得、詳細取得）
- 進捗管理API（進捗取得、完了マーク、状態チェック、完了削除）
- ヘルスチェックAPI
- 進捗率を整数パーセンテージ（0~100%）に変更
