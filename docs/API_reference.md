# API リファレンス

J15 Backend の REST API を最もシンプルに把握できるよう、エンドポイント一覧と代表的な JSON のみをまとめたリファレンスです。トークンの取り扱いやバリデーションの細則は `docs/API_v2.md` を参照してください。

## 基本情報

| 項目         | 内容                                                          |
| ------------ | ------------------------------------------------------------- |
| 本番 URL     | `https://zu9mkxoir4.execute-api.ap-northeast-1.amazonaws.com` |
| ローカル URL | `http://localhost:8080`                                       |
| 認証         | `Authorization: Bearer <accessToken>`（ログイン後に取得）     |

アクセストークンが失効したら `/api/auth/refresh` にリフレッシュトークンを送信して再発行します。

---

# エンドポイントと JSON

以下、カテゴリー別に「メソッド + パス」とリクエスト/レスポンス JSON を抜粋しています。

## 認証

### サインアップ

- `POST /api/auth/signup`（認証不要）

```jsonc
// Request
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "pass1234"
}

// 201 Response
{
  "accessToken": "...",
  "refreshToken": "...",
  "user": {
    "id": "uuid",
    "username": "testuser",
    "email": "test@example.com"
  }
}
```

### サインイン

- `POST /api/auth/signin`（認証不要）

```jsonc
// Request
{
  "email": "test@example.com",
  "password": "pass1234"
}

// 200 Response
{
  "accessToken": "...",
  "refreshToken": "...",
  "user": { "id": "uuid", "username": "testuser", "email": "test@example.com" }
}
```

### トークンリフレッシュ

- `POST /api/auth/refresh`（認証不要）

```jsonc
// Request
{ "refreshToken": "..." }

// 200 Response
{ "accessToken": "...", "refreshToken": "..." }
```

---

## 題材

### 題材取得

- `GET /api/subjects`（認証不要, 200）
- `GET /api/subjects/{subjectId}`（認証不要, 200/404）

```jsonc
// Response (一覧例)
[
  {
    "subjectId": 1,
    "title": "デフォルト題材",
    "description": "...",
    "maxSections": 101
  }
]
```

### 題材作成/更新/削除

- `POST /api/subjects` `ROLE_ADMIN`
- `PUT /api/subjects/{subjectId}` `ROLE_ADMIN`
- `DELETE /api/subjects/{subjectId}` `ROLE_ADMIN`

```jsonc
// Request (POST/PUT)
{
  "subjectId": 2,
  "title": "新題材",
  "description": "説明",
  "maxSections": 50
}
```

## セクション

### セクション取得

- `GET /api/subjects/{subjectId}/sections`
- `GET /api/subjects/{subjectId}/sections/{sectionId}`

```jsonc
// Response
{
  "subjectId": 1,
  "sectionId": 10,
  "title": "環境構築",
  "description": "...",
  "image": "..."
}
```

### セクション作成

- `POST /api/subjects/{subjectId}/sections` `ROLE_ADMIN`

```jsonc
// Request
{
  "sectionId": 10,
  "title": "セクションタイトル",
  "description": "説明",
  "image": "base64 string or url"
}

// 201 Response
{
  "subjectId": 1,
  "sectionId": 10,
  "title": "セクションタイトル",
  "description": "説明",
  "image": "..."
}
```

### セクション更新

- `PUT /api/subjects/{subjectId}/sections/{sectionId}` `ROLE_ADMIN`

```jsonc
// Request
{
  "title": "更新タイトル",
  "description": "更新説明",
  "image": "base64 string or url"
}

// 200 Response
{
  "subjectId": 1,
  "sectionId": 10,
  "title": "更新タイトル",
  "description": "更新説明",
  "image": "..."
}
```

### セクション削除

- `DELETE /api/subjects/{subjectId}/sections/{sectionId}` `ROLE_ADMIN`

```jsonc
// 200 Response
{
  "message": "セクション 10 を削除しました"
}
```

---

## 進捗（認証必須）

### 進捗取得

- `GET /api/progress/subjects/{subjectId}`

```jsonc
{
  "subjectId": 1,
  "userId": "uuid",
  "completedSections": [0, 10, 20],
  "progressRate": 0.3
}
```

### セクション完了マーク

- `POST /api/progress/subjects/{subjectId}/sections`

```jsonc
// Request
{ "sectionId": 30 }

// 201 Response
{
  "message": "セクション 30 を完了としてマークしました",
  "sectionId": 30,
  "completedAt": "2025-12-10T11:00:00Z"
}
```

### 完了状態チェック / 解除

- `GET /api/progress/subjects/{subjectId}/sections/{sectionId}`
- `DELETE /api/progress/subjects/{subjectId}/sections/{sectionId}`

```jsonc
// GET Response
{ "isCleared": true }

// DELETE Response
{ "message": "セクション 30 の完了記録を削除しました" }
```

---

## 管理者

### 管理者ユーザー作成

- `POST /api/admin/users`
- ヘッダー `X-Admin-Key: <ADMIN_API_KEY>`

```jsonc
// Request
{
  "email": "admin@example.com",
  "username": "adminuser",
  "password": "adminpass123"
}

// 201 Response
{
  "userId": "uuid",
  "email": "admin@example.com",
  "username": "adminuser",
  "role": "ROLE_ADMIN"
}
```

このドキュメントはリクエスト/レスポンスの雛形として利用し、より詳細な仕様は `API_v2.md` を参照してください。
