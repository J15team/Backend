# 管理者向け進捗ダッシュボード API

## 概要

管理者が全ユーザーの課題進捗状況を一覧で確認できる API です。

## エンドポイント

### GET /api/admin/progress/assignments

全ユーザーの課題題材進捗を取得します。

#### 認証

- **必須**: Bearer Token
- **権限**: ADMIN ロールが必要

#### リクエスト

```http
GET /api/admin/progress/assignments
Authorization: Bearer <admin_token>
```

#### レスポンス

```json
{
  "users": [
    {
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "username": "tanaka",
      "email": "tanaka@example.com",
      "subjects": [
        {
          "subjectId": 1,
          "title": "C言語入門",
          "progressPercent": 66,
          "clearedSections": 2,
          "totalSections": 3,
          "isCleared": false
        },
        {
          "subjectId": 2,
          "title": "アルゴリズム基礎",
          "progressPercent": 100,
          "clearedSections": 5,
          "totalSections": 5,
          "isCleared": true
        }
      ]
    },
    {
      "userId": "660e8400-e29b-41d4-a716-446655440001",
      "username": "suzuki",
      "email": "suzuki@example.com",
      "subjects": [
        {
          "subjectId": 1,
          "title": "C言語入門",
          "progressPercent": 33,
          "clearedSections": 1,
          "totalSections": 3,
          "isCleared": false
        },
        {
          "subjectId": 2,
          "title": "アルゴリズム基礎",
          "progressPercent": 0,
          "clearedSections": 0,
          "totalSections": 5,
          "isCleared": false
        }
      ]
    }
  ]
}
```

#### レスポンスフィールド

| フィールド                   | 型      | 説明                   |
| ---------------------------- | ------- | ---------------------- |
| `users`                      | array   | ユーザー進捗リスト     |
| `users[].userId`             | string  | ユーザー ID (UUID)     |
| `users[].username`           | string  | ユーザー名             |
| `users[].email`              | string  | メールアドレス         |
| `users[].subjects`           | array   | 題材進捗リスト         |
| `subjects[].subjectId`       | number  | 課題題材 ID            |
| `subjects[].title`           | string  | 題材タイトル           |
| `subjects[].progressPercent` | number  | 進捗率 (0-100)         |
| `subjects[].clearedSections` | number  | クリア済みセクション数 |
| `subjects[].totalSections`   | number  | 課題ありセクション総数 |
| `subjects[].isCleared`       | boolean | 題材クリア済みフラグ   |

#### 進捗計算ロジック

- **セクションクリア条件**: 最高スコアが 100 点
- **進捗率**: `(クリア済みセクション数 / 課題ありセクション総数) * 100`
- **題材クリア条件**: 全ての課題ありセクションをクリア
- **課題なし題材**: `progressPercent: 100`, `isCleared: true`, `totalSections: 0`

#### エラーレスポンス

**401 Unauthorized** - 認証なし

```json
{
  "error": "Unauthorized",
  "message": "認証が必要です"
}
```

**403 Forbidden** - ADMIN 権限なし

```json
{
  "error": "Forbidden",
  "message": "管理者権限が必要です"
}
```

---

## フロントエンド実装例

### TypeScript 型定義

```typescript
interface SubjectProgressSummary {
  subjectId: number;
  title: string;
  progressPercent: number;
  clearedSections: number;
  totalSections: number;
  isCleared: boolean;
}

interface UserProgressSummary {
  userId: string;
  username: string;
  email: string;
  subjects: SubjectProgressSummary[];
}

interface AdminAssignmentProgressResponse {
  users: UserProgressSummary[];
}
```

### API 呼び出し例

```typescript
const fetchAdminProgress = async (
  token: string
): Promise<AdminAssignmentProgressResponse> => {
  const response = await fetch("/api/admin/progress/assignments", {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  return response.json();
};
```

### 表示例（テーブル形式）

| ユーザー | C 言語入門 | アルゴリズム基礎 |
| -------- | ---------- | ---------------- |
| tanaka   | 66% (2/3)  | ✅ 100% (5/5)    |
| suzuki   | 33% (1/3)  | 0% (0/5)         |

---

## 注意事項

1. **パフォーマンス**: 全ユーザー・全題材を取得するため、ユーザー数が多い場合はレスポンスが大きくなります
2. **キャッシュ推奨**: 頻繁に更新されないデータのため、フロントエンドでのキャッシュを推奨
3. **権限チェック**: ADMIN 以外のユーザーがアクセスすると 403 エラー
