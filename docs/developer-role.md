# ROLE_DEVELOPER（開発者ロール）

## 概要

開発者専用の最上位権限ロール。全ての操作が可能で、他のロールからは操作されない「無敵」の存在。

## 権限階層

```
ROLE_DEVELOPER (最上位)
├── 全エンドポイントへのアクセス
├── 任意ユーザーの削除（Developer以外）
├── エンドポイント一覧の取得
└── 他ロールからの保護

ROLE_ADMIN (中位)
├── 題材・タグ・セクションの管理
├── 他Adminユーザーの管理
└── Developerへの操作は不可

ROLE_USER (一般)
└── 自分の進捗のみ操作可能
```

## 開発者専用エンドポイント

### 認証

全ての開発者エンドポイントは `X-Dev-Key` ヘッダーで認証。

```
X-Dev-Key: <DEV_API_KEY環境変数の値>
```

### GET /api/dev/verify

開発者かどうかを確認。フロントエンドで開発者専用ページの表示制御に使用。

**レスポンス:**

```json
// 有効なX-Dev-Key
{ "isDeveloper": true }

// 無効または未設定
{ "isDeveloper": false }
```

### GET /api/dev/endpoints

全 API エンドポイント一覧を取得。

**レスポンス:**

```json
{
  "endpoints": [
    {
      "method": "GET",
      "path": "/api/health",
      "description": "HealthController.health()"
    },
    {
      "method": "POST",
      "path": "/api/auth/signin",
      "description": "AuthController.signin()"
    }
  ],
  "count": 2
}
```

### DELETE /api/dev/users/{userId}

任意のユーザーを削除。ROLE_DEVELOPER は削除不可。

**レスポンス:**

- 204 No Content: 削除成功
- 403 Forbidden: ROLE_DEVELOPER を削除しようとした場合
- 404 Not Found: ユーザーが存在しない場合

## 環境変数

| 変数名        | 説明                                                             |
| ------------- | ---------------------------------------------------------------- |
| `DEV_API_KEY` | 開発者認証キー。未設定の場合、開発者エンドポイントは無効化される |

## 開発者保護

- ROLE_ADMIN は ROLE_DEVELOPER を更新・削除できない
- `getAllAdminUsers()`は ROLE_DEVELOPER を含まない
- ROLE_DEVELOPER の操作は ROLE_DEVELOPER のみ可能
