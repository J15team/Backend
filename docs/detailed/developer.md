# 開発者 API 詳細仕様書

## 概要

開発者専用の API エンドポイント。`X-Dev-Key`ヘッダーによる認証が必要。
フロントエンドで開発者専用ページの表示制御に使用する。

## 認証

| ヘッダー    | 説明                                               |
| ----------- | -------------------------------------------------- |
| `X-Dev-Key` | 開発者専用 API キー（環境変数`DEV_API_KEY`で設定） |

## ロール階層

```
ROLE_DEVELOPER (最上位)
    ├── 全エンドポイントへのアクセス
    ├── 任意ユーザーの削除（Developer以外）
    ├── エンドポイント一覧の取得
    └── 他ロールからの保護（更新・削除不可）

ROLE_ADMIN (中位)
    ├── 題材・タグ・セクションの管理
    ├── 他Adminユーザーの管理
    └── Developerへの操作は不可

ROLE_USER (一般)
    └── 自分の進捗のみ操作可能
```

---

## エンドポイント一覧

| メソッド | パス                      | 認証                | 説明                   |
| -------- | ------------------------- | ------------------- | ---------------------- |
| GET      | `/api/dev/verify`         | `X-Dev-Key`（任意） | 開発者確認             |
| GET      | `/api/dev/endpoints`      | `X-Dev-Key`         | エンドポイント一覧取得 |
| DELETE   | `/api/dev/users/{userId}` | `X-Dev-Key`         | ユーザー削除           |

---

## エンドポイント詳細

### GET /api/dev/verify

開発者かどうかを確認する。フロントエンドで開発者専用ページの表示制御に使用。

#### リクエスト

```http
GET /api/dev/verify
X-Dev-Key: <dev-api-key>  # 任意
```

#### レスポンス

**成功時（有効な X-Dev-Key）**

```json
{
  "isDeveloper": true
}
```

**失敗時（無効または未設定の X-Dev-Key）**

```json
{
  "isDeveloper": false
}
```

#### ステータスコード

| コード | 説明                               |
| ------ | ---------------------------------- |
| 200    | 常に成功（isDeveloper の値で判定） |

---

### GET /api/dev/endpoints

全 API エンドポイントの一覧を取得する。

#### リクエスト

```http
GET /api/dev/endpoints
X-Dev-Key: <dev-api-key>
```

#### レスポンス

**成功時**

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
  "count": 42
}
```

**エラー時**

```json
{
  "error": "X-Dev-Key header is required"
}
```

#### ステータスコード

| コード | 説明                                   |
| ------ | -------------------------------------- |
| 200    | 成功                                   |
| 401    | X-Dev-Key 未設定または無効             |
| 503    | 開発者機能が無効（DEV_API_KEY 未設定） |

---

### DELETE /api/dev/users/{userId}

指定したユーザーを削除する。`ROLE_DEVELOPER`ユーザーは削除不可。

#### リクエスト

```http
DELETE /api/dev/users/550e8400-e29b-41d4-a716-446655440000
X-Dev-Key: <dev-api-key>
```

#### パスパラメータ

| パラメータ | 型   | 説明                  |
| ---------- | ---- | --------------------- |
| userId     | UUID | 削除対象のユーザー ID |

#### レスポンス

**成功時**

```
204 No Content
```

**エラー時**

```json
{
  "error": "開発者ユーザーは削除できません"
}
```

#### ステータスコード

| コード | 説明                                   |
| ------ | -------------------------------------- |
| 204    | 削除成功                               |
| 401    | X-Dev-Key 未設定または無効             |
| 403    | ROLE_DEVELOPER ユーザーの削除を試みた  |
| 404    | ユーザーが見つからない                 |
| 503    | 開発者機能が無効（DEV_API_KEY 未設定） |

---

## 実装例

### TypeScript/JavaScript

```typescript
const DEV_KEY = process.env.DEV_API_KEY;

// 開発者確認
const verifyDeveloper = async (): Promise<boolean> => {
  const res = await fetch("/api/dev/verify", {
    headers: DEV_KEY ? { "X-Dev-Key": DEV_KEY } : {},
  });
  const data = await res.json();
  return data.isDeveloper;
};

// エンドポイント一覧取得
const getEndpoints = async () => {
  const res = await fetch("/api/dev/endpoints", {
    headers: { "X-Dev-Key": DEV_KEY },
  });
  if (!res.ok) {
    throw new Error("Failed to fetch endpoints");
  }
  return await res.json();
};

// ユーザー削除
const deleteUser = async (userId: string) => {
  const res = await fetch(`/api/dev/users/${userId}`, {
    method: "DELETE",
    headers: { "X-Dev-Key": DEV_KEY },
  });
  if (res.status === 403) {
    throw new Error("Cannot delete developer user");
  }
  if (res.status === 404) {
    throw new Error("User not found");
  }
  if (!res.ok) {
    throw new Error("Failed to delete user");
  }
};

// フロントエンドでの開発者ページ表示制御
const DeveloperPage = () => {
  const [isDeveloper, setIsDeveloper] = useState(false);

  useEffect(() => {
    verifyDeveloper().then(setIsDeveloper);
  }, []);

  if (!isDeveloper) {
    return <Navigate to="/" />;
  }

  return <DeveloperDashboard />;
};
```

---

## 環境変数

| 変数名        | 説明            | 必須                           |
| ------------- | --------------- | ------------------------------ |
| `DEV_API_KEY` | 開発者 API キー | 開発者機能を使用する場合は必須 |

**注意**: `DEV_API_KEY`が未設定の場合、全ての開発者エンドポイントは 503 を返す（`/api/dev/verify`は`isDeveloper: false`を返す）。

---

## セキュリティ

- `X-Dev-Key`は定数時間比較で検証（タイミング攻撃対策）
- キーの長さは 1000 文字以下に制限（DoS 対策）
- `ROLE_DEVELOPER`ユーザーは他のロールから更新・削除不可
- `DEV_API_KEY`はログや API レスポンスに露出しない
