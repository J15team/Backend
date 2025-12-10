# J15 Backend API 仕様書 v2.0

## 📋 目次

- [概要](#概要)
- [共通仕様](#共通仕様)
- [認証 API](#認証api)
- [題材管理 API](#題材管理api)
- [セクション管理 API](#セクション管理api)
- [進捗管理 API](#進捗管理api)
- [エラーレスポンス](#エラーレスポンス)

---

## 概要

### ベース URL

**本番環境 (AWS)**

```
https://zu9mkxoir4.execute-api.ap-northeast-1.amazonaws.com
```

**ローカル開発環境**

```
http://localhost:8080
```

### 認証

本APIはJWTトークンによる認証を実装しています。

**認証が必要なエンドポイント**
- 進捗管理API（`/api/progress/**`）
- 題材の作成・更新・削除

**認証方法**

リクエストヘッダーに以下を含めます：

```
Authorization: Bearer {accessToken}
```

**トークンの取得**

サインインAPIでアクセストークンとリフレッシュトークンを取得できます。

---

## 共通仕様

### レスポンス形式

すべてのレスポンスは JSON 形式です。

### 日時形式

ISO 8601 形式（UTC）を使用します。

```
例: "2025-12-06T10:00:00Z"
```

### HTTP ステータスコード

| コード | 説明                         |
| ------ | ---------------------------- |
| 200    | 成功（取得・更新・削除）     |
| 201    | 成功（作成）                 |
| 204    | 成功（レスポンスボディなし） |
| 400    | リクエストが不正             |
| 401    | 認証が必要                   |
| 403    | アクセス権限なし             |
| 404    | リソースが見つからない       |
| 409    | リソースの競合（重複など）   |
| 500    | サーバーエラー               |

---

## 認証 API

### サインアップ

新規ユーザーを登録します。

**エンドポイント**

```http
POST /api/auth/signup
```

**認証**: 不要

**リクエストヘッダー**

```
Content-Type: application/json
```

**リクエストボディ**

```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}
```

| フィールド | 型     | 必須 | 説明           | バリデーション                        |
| ---------- | ------ | ---- | -------------- | ------------------------------------- |
| username   | string | ○    | ユーザー名     | 3~50 文字、英数字とアンダースコアのみ |
| email      | string | ○    | メールアドレス | メール形式、一意制約                  |
| password   | string | ○    | パスワード     | 8 文字以上                            |

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

**エラー**

- **400 Bad Request**: バリデーションエラー
- **409 Conflict**: メールアドレスまたはユーザー名が既に存在

---

### サインイン

ユーザー認証を行います。

**エンドポイント**

```http
POST /api/auth/signin
```

**認証**: 不要

**リクエストヘッダー**

```
Content-Type: application/json
```

**リクエストボディ**

```json
{
  "email": "test@example.com",
  "password": "password123"
}
```

| フィールド | 型     | 必須 | 説明           |
| ---------- | ------ | ---- | -------------- |
| email      | string | ○    | メールアドレス |
| password   | string | ○    | パスワード     |

**レスポンス**

**成功時 (200 OK)**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "testuser",
    "email": "test@example.com"
  }
}
```

| フィールド   | 型     | 説明                                     |
| ------------ | ------ | ---------------------------------------- |
| accessToken  | string | アクセストークン（JWT）、APIリクエストに使用 |
| refreshToken | string | リフレッシュトークン（JWT）、トークン更新に使用 |
| user         | object | ユーザー情報                             |
| user.id      | string | ユーザーID（UUID形式）                   |
| user.username | string | ユーザー名                               |
| user.email   | string | メールアドレス                           |

**エラー**

- **400 Bad Request**: メールアドレスまたはパスワードが不正

---

### トークンリフレッシュ

```
POST /api/auth/refresh
```

**認証**: 不要

**リクエストヘッダー**

```
Content-Type: application/json
```

**リクエストボディ**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

| フィールド   | 型     | 必須 | 説明                   |
| ------------ | ------ | ---- | ---------------------- |
| refreshToken | string | ○    | リフレッシュトークン   |

**レスポンス**

**成功時 (200 OK)**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

| フィールド   | 型     | 説明                                     |
| ------------ | ------ | ---------------------------------------- |
| accessToken  | string | 新しいアクセストークン（JWT）             |
| refreshToken | string | リフレッシュトークン（JWT）               |

**エラー**

- **400 Bad Request**: リフレッシュトークンが無効または期限切れ
- **400 Bad Request**: ユーザーが見つからない

---

## 題材管理 API

題材（学習プロジェクト）を管理します。各題材には複数のセクションが属します。

### 題材一覧取得

すべての題材を取得します。

**エンドポイント**

```http
GET /api/subjects
```

**認証**: 不要

**レスポンス**

**成功時 (200 OK)**

```json
[
  {
    "subjectId": 1,
    "title": "デフォルト題材",
    "description": "マイグレーションで作成されたデフォルト題材",
    "maxSections": 101,
    "createdAt": "2025-12-06T10:00:00Z"
  },
  {
    "subjectId": 2,
    "title": "React入門",
    "description": "Reactの基礎を学ぶ",
    "maxSections": 50,
    "createdAt": "2025-12-06T11:00:00Z"
  }
]
```

---

### 題材詳細取得

特定の題材の詳細を取得します。

**エンドポイント**

```http
GET /api/subjects/{subjectId}
```

**パスパラメータ**

| パラメータ | 型   | 説明    |
| ---------- | ---- | ------- |
| subjectId  | long | 題材 ID |

**レスポンス**

**成功時 (200 OK)**

```json
{
  "subjectId": 1,
  "title": "デフォルト題材",
  "description": "マイグレーションで作成されたデフォルト題材",
  "maxSections": 101,
  "createdAt": "2025-12-06T10:00:00Z"
}
```

**エラー**

- **404 Not Found**: 題材が存在しない

---

### 題材作成

新しい題材を作成します。

**エンドポイント**

```http
POST /api/subjects
```

**認証**: 不要（将来的に必要）

**リクエストヘッダー**

```
Content-Type: application/json
```

**リクエストボディ**

```json
{
  "subjectId": 100,
  "title": "TypeScript入門",
  "description": "TypeScriptの基礎を学ぶ",
  "maxSections": 50
}
```

| フィールド  | 型     | 必須 | 説明             | バリデーション |
| ----------- | ------ | ---- | ---------------- | -------------- |
| subjectId   | long   | ○    | 題材 ID          | 一意制約       |
| title       | string | ○    | 題材のタイトル   | 1 文字以上     |
| description | string | ×    | 題材の説明       | -              |
| maxSections | int    | ○    | 最大セクション数 | 1~1000         |

**レスポンス**

**成功時 (201 Created)**

```json
{
  "subjectId": 100,
  "title": "TypeScript入門",
  "description": "TypeScriptの基礎を学ぶ",
  "maxSections": 50,
  "createdAt": "2025-12-06T12:00:00Z"
}
```

**エラー**

- **400 Bad Request**: バリデーションエラー
- **409 Conflict**: 題材 ID が既に存在

---

### 題材更新

既存の題材を更新します。

**エンドポイント**

```http
PUT /api/subjects/{subjectId}
```

**パスパラメータ**

| パラメータ | 型   | 説明    |
| ---------- | ---- | ------- |
| subjectId  | long | 題材 ID |

**リクエストヘッダー**

```
Content-Type: application/json
```

**リクエストボディ**

```json
{
  "title": "TypeScript入門（改訂版）",
  "description": "TypeScriptの基礎から応用まで",
  "maxSections": 60
}
```

| フィールド  | 型     | 必須 | 説明             | バリデーション |
| ----------- | ------ | ---- | ---------------- | -------------- |
| title       | string | ○    | 題材のタイトル   | 1 文字以上     |
| description | string | ×    | 題材の説明       | -              |
| maxSections | int    | ○    | 最大セクション数 | 1~1000         |

**レスポンス**

**成功時 (200 OK)**

```json
{
  "subjectId": 100,
  "title": "TypeScript入門（改訂版）",
  "description": "TypeScriptの基礎から応用まで",
  "maxSections": 60,
  "createdAt": "2025-12-06T12:00:00Z"
}
```

**エラー**

- **400 Bad Request**: バリデーションエラー
- **404 Not Found**: 題材が存在しない

---

### 題材削除

題材を削除します。

**エンドポイント**

```http
DELETE /api/subjects/{subjectId}
```

**パスパラメータ**

| パラメータ | 型   | 説明    |
| ---------- | ---- | ------- |
| subjectId  | long | 題材 ID |

**レスポンス**

**成功時 (204 No Content)**

レスポンスボディなし

**エラー**

- **404 Not Found**: 題材が存在しない

---

## セクション管理 API

各題材に属するセクション（学習ステップ）を管理します。

### セクション一覧取得

特定の題材に属する全セクションを取得します。

**エンドポイント**

```http
GET /api/subjects/{subjectId}/sections
```

**パスパラメータ**

| パラメータ | 型   | 説明    |
| ---------- | ---- | ------- |
| subjectId  | long | 題材 ID |

**レスポンス**

**成功時 (200 OK)**

```json
[
  {
    "subjectId": 1,
    "sectionId": 0,
    "title": "プロジェクト準備",
    "description": "環境構築とプロジェクトのセットアップ"
  },
  {
    "subjectId": 1,
    "sectionId": 1,
    "title": "基本機能の実装",
    "description": "CRUDの基本機能を実装する"
  }
]
```

**エラー**

- **404 Not Found**: 題材が存在しない

---

### セクション詳細取得

特定のセクションの詳細を取得します。

**エンドポイント**

```http
GET /api/subjects/{subjectId}/sections/{sectionId}
```

**パスパラメータ**

| パラメータ | 型   | 説明                   |
| ---------- | ---- | ---------------------- |
| subjectId  | long | 題材 ID                |
| sectionId  | int  | セクション ID（0~100） |

**レスポンス**

**成功時 (200 OK)**

```json
{
  "subjectId": 1,
  "sectionId": 0,
  "title": "プロジェクト準備",
  "description": "環境構築とプロジェクトのセットアップ"
}
```

**エラー**

- **404 Not Found**: 題材またはセクションが存在しない

---

## 進捗管理 API

ユーザーの学習進捗を管理します。各ユーザーが題材ごとに完了したセクションを記録・取得できます。

### 進捗状態取得

認証済みユーザーの特定題材における進捗状態を取得します。

**エンドポイント**

```http
GET /api/progress/subjects/{subjectId}
```

**認証**: 必須（JWTトークン）

**リクエストヘッダー**

```
Authorization: Bearer {accessToken}
```

**パスパラメータ**

| パラメータ | 型   | 説明    |
| ---------- | ---- | ------- |
| subjectId  | long | 題材 ID |

**レスポンス**

**成功時 (200 OK)**

```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "subjectId": 1,
  "progressPercentage": 15,
  "clearedCount": 16,
  "remainingCount": 85,
  "totalSections": 101,
  "isAllCleared": false,
  "nextSectionId": 16,
  "clearedSections": [
    {
      "sectionId": 0,
      "completedAt": "2025-12-06T10:00:00Z"
    },
    {
      "sectionId": 1,
      "completedAt": "2025-12-06T11:00:00Z"
    }
  ]
}
```

**レスポンスフィールド説明**

| フィールド         | 型          | 説明                                           |
| ------------------ | ----------- | ---------------------------------------------- |
| userId             | string      | ユーザー ID（UUID 形式）                       |
| subjectId          | long        | 題材 ID                                        |
| progressPercentage | int         | 進捗率（0~100%）小数点以下切り捨て             |
| clearedCount       | int         | 完了済みセクション数                           |
| remainingCount     | int         | 未完了セクション数                             |
| totalSections      | int         | 題材の総セクション数（maxSections）            |
| isAllCleared       | boolean     | 全セクション完了しているか                     |
| nextSectionId      | int or null | 次に完了すべきセクション ID（全完了時は null） |
| clearedSections    | array       | 完了済みセクションの詳細リスト                 |

**エラー**

- **401 Unauthorized**: 認証トークンが無効または期限切れ
- **404 Not Found**: 題材が存在しない

**フロントエンド実装例**

```typescript
// 進捗バーの表示（認証トークンを含める）
const progress = await fetch(`/api/progress/subjects/${subjectId}`, {
  headers: {
    'Authorization': `Bearer ${accessToken}`
  }
});
const data = await progress.json();

// プログレスバーに表示
<ProgressBar value={data.progressPercentage} />
<Text>{data.clearedCount} / {data.totalSections} セクション完了</Text>

// 次のセクションを提案
if (data.nextSectionId !== null) {
  <Button>次のセクション（{data.nextSectionId}）を開始</Button>
}
```

---

### セクション完了記録

認証済みユーザーがセクションを完了したことを記録します。

**エンドポイント**

```http
POST /api/progress/subjects/{subjectId}/sections
```

**認証**: 必須（JWTトークン）

**パスパラメータ**

| パラメータ | 型   | 説明    |
| ---------- | ---- | ------- |
| subjectId  | long | 題材 ID |

**リクエストヘッダー**

```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**リクエストボディ**

```json
{
  "sectionId": 5
}
```

| フィールド | 型  | 必須 | 説明          | バリデーション |
| ---------- | --- | ---- | ------------- | -------------- |
| sectionId  | int | ○    | セクション ID | 0~100          |

**レスポンス**

**成功時 (201 Created)**

```json
{
  "message": "セクション 5 を完了としてマークしました",
  "sectionId": 5,
  "completedAt": "2025-12-06T12:30:00Z"
}
```

**エラー**

- **400 Bad Request**: セクション ID が不正、または既に完了済み
  ```json
  {
    "error": "セクション 5 は既に完了しています"
  }
  ```
- **401 Unauthorized**: 認証トークンが無効または期限切れ
- **404 Not Found**: 題材が存在しない

**フロントエンド実装例**

```typescript
// セクション完了時に呼び出す（認証トークンを含める）
async function markSectionComplete(sectionId: number) {
  const response = await fetch(
    `/api/progress/subjects/${subjectId}/sections`,
    {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${accessToken}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ sectionId }),
    }
  );

  if (response.status === 201) {
    // 成功: 進捗バーを更新
    refreshProgress();
  } else if (response.status === 400) {
    // 既に完了済み
    const error = await response.json();
    alert(error.error);
  }
}
```

---

### セクション完了状態チェック

認証済みユーザーの特定セクションが完了済みかチェックします。

**エンドポイント**

```http
GET /api/progress/subjects/{subjectId}/sections/{sectionId}
```

**認証**: 必須（JWTトークン）

**リクエストヘッダー**

```
Authorization: Bearer {accessToken}
```

**パスパラメータ**

| パラメータ | 型   | 説明                   |
| ---------- | ---- | ---------------------- |
| subjectId  | long | 題材 ID                |
| sectionId  | int  | セクション ID（0~100） |

**レスポンス**

**成功時 (200 OK)**

```json
{
  "isCleared": true
}
```

| フィールド | 型      | 説明                          |
| ---------- | ------- | ----------------------------- |
| isCleared  | boolean | true: 完了済み、false: 未完了 |

**エラー**

- **401 Unauthorized**: 認証トークンが無効または期限切れ

**フロントエンド実装例**

```typescript
// セクションにチェックマークを表示するか判定（認証トークンを含める）
const result = await fetch(
  `/api/progress/subjects/${subjectId}/sections/${sectionId}`,
  {
    headers: {
      'Authorization': `Bearer ${accessToken}`
    }
  }
);
const { isCleared } = await result.json();

if (isCleared) {
  // チェックマークを表示
  <CheckIcon />;
}
```

---

### セクション完了削除（デバッグ用）

認証済みユーザーのセクション完了記録を削除します。

**エンドポイント**

```http
DELETE /api/progress/subjects/{subjectId}/sections/{sectionId}
```

**認証**: 必須（JWTトークン）

**リクエストヘッダー**

```
Authorization: Bearer {accessToken}
```

**パスパラメータ**

| パラメータ | 型   | 説明                   |
| ---------- | ---- | ---------------------- |
| subjectId  | long | 題材 ID                |
| sectionId  | int  | セクション ID（0~100） |

**レスポンス**

**成功時 (200 OK)**

```json
{
  "message": "セクション 5 の完了記録を削除しました"
}
```

**エラー**

- **400 Bad Request**: 削除に失敗
  ```json
  {
    "error": "削除に失敗しました"
  }
  ```
- **401 Unauthorized**: 認証トークンが無効または期限切れ

---

## エラーレスポンス

### 共通エラー形式

エラーレスポンスは以下の形式で返されます。

```json
{
  "error": "エラーメッセージ"
}
```

または詳細がある場合：

```json
{
  "error": "Validation failed",
  "details": [
    "Username must be between 3 and 50 characters",
    "Password must be at least 8 characters"
  ]
}
```

### HTTP ステータスコードとエラー内容

| ステータスコード          | 説明                   | よくある原因                     |
| ------------------------- | ---------------------- | -------------------------------- |
| 400 Bad Request           | リクエストが不正       | バリデーションエラー、重複操作   |
| 404 Not Found             | リソースが見つからない | 存在しない ID、削除済みリソース  |
| 409 Conflict              | リソースの競合         | メールアドレス重複、題材 ID 重複 |
| 500 Internal Server Error | サーバーエラー         | 予期しないエラー                 |

---

## フロントエンド実装ガイド

### 基本的な実装フロー

#### 1. ユーザー登録・ログイン

```typescript
// サインアップ
const signup = async (username: string, email: string, password: string) => {
  const response = await fetch("/api/auth/signup", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, email, password }),
  });

  if (response.status === 201) {
    const user = await response.json();
    localStorage.setItem("userId", user.userId);
    return user;
  }
};

// サインイン
const signin = async (email: string, password: string) => {
  const response = await fetch("/api/auth/signin", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });

  if (response.ok) {
    const user = await response.json();
    localStorage.setItem("userId", user.userId);
    return user;
  }
};
```

#### 2. 題材一覧取得と表示

```typescript
const fetchSubjects = async () => {
  const response = await fetch("/api/subjects");
  const subjects = await response.json();
  return subjects;
};

// 表示
subjects.map((subject) => (
  <Card key={subject.subjectId}>
    <Title>{subject.title}</Title>
    <Description>{subject.description}</Description>
    <Text>全 {subject.maxSections} セクション</Text>
    <Button onClick={() => selectSubject(subject.subjectId)}>学習を開始</Button>
  </Card>
));
```

#### 3. セクション一覧と進捗の表示

```typescript
const fetchSectionsWithProgress = async (userId: string, subjectId: number) => {
  // セクション一覧を取得
  const sectionsRes = await fetch(`/api/subjects/${subjectId}/sections`);
  const sections = await sectionsRes.json();

  // 進捗を取得
  const progressRes = await fetch(`/api/progress/${userId}/subjects/${subjectId}`);
  const progress = await progressRes.json();

  // セクションに完了状態をマージ
  const sectionsWithStatus = sections.map(section => ({
    ...section,
    isCleared: progress.clearedSections.some(
      cs => cs.sectionId === section.sectionId
    )
  }));

  return { sections: sectionsWithStatus, progress };
};

// 表示
<ProgressBar value={progress.progressPercentage} />
<Text>{progress.clearedCount} / {progress.totalSections} 完了</Text>

{sections.map(section => (
  <SectionItem key={section.sectionId}>
    <Text>{section.title}</Text>
    {section.isCleared && <CheckIcon />}
    <Button onClick={() => toggleComplete(section.sectionId)}>
      {section.isCleared ? '未完了にする' : '完了にする'}
    </Button>
  </SectionItem>
))}
```

#### 4. セクション完了の切り替え

```typescript
const toggleComplete = async (sectionId: number, isCleared: boolean) => {
  if (isCleared) {
    // 完了を削除
    await fetch(
      `/api/progress/${userId}/subjects/${subjectId}/sections/${sectionId}`,
      { method: "DELETE" }
    );
  } else {
    // 完了をマーク
    await fetch(`/api/progress/${userId}/subjects/${subjectId}/sections`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ sectionId }),
    });
  }

  // 進捗を再取得
  refreshProgress();
};
```

#### 5. 題材の作成（管理者機能）

```typescript
const createSubject = async (data: {
  subjectId: number;
  title: string;
  description: string;
  maxSections: number;
}) => {
  const response = await fetch("/api/subjects", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });

  if (response.status === 201) {
    const subject = await response.json();
    return subject;
  }
};
```

### データ取得タイミングの推奨

| 画面             | 取得するデータ           | タイミング       |
| ---------------- | ------------------------ | ---------------- |
| ホーム画面       | 題材一覧                 | マウント時       |
| 題材詳細画面     | セクション一覧、進捗状態 | 題材選択時       |
| セクション完了後 | 進捗状態                 | 完了ボタン押下後 |
| ページ遷移時     | なし                     | キャッシュを活用 |

### エラーハンドリング

```typescript
const apiCall = async (url: string, options?: RequestInit) => {
  try {
    const response = await fetch(url, options);

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.error || "エラーが発生しました");
    }

    return await response.json();
  } catch (error) {
    console.error("API Error:", error);
    // ユーザーにエラーメッセージを表示
    showErrorToast(error.message);
    throw error;
  }
};
```

---

## 補足情報

### 進捗率計算について

- **計算式**: `(完了セクション数 × 100) ÷ 題材の最大セクション数`
- **小数点処理**: 切り捨て（0~99 の範囲）
- **全完了時**: 必ず 100%を返す
- **例**: 16 セクション完了、最大 101 セクション → `(16 × 100) ÷ 101 = 15%`

### セクション ID の範囲

- **最小**: 0
- **最大**: 100
- 各題材は 0 から始まる連番である必要はありません
- フロントエンド側で任意のセクション ID を使用可能

### 題材とセクションの関係

```
Subject (題材)
  ├── maxSections: 101 (この題材の最大セクション数)
  └── Sections (セクション一覧)
      ├── Section 0
      ├── Section 1
      ├── Section 2
      └── ...
```

- 各題材は独立したセクション群を持つ
- セクション ID は題材内でのみ一意
- 異なる題材で同じセクション ID を使用可能

---

## 変更履歴

| バージョン | 日付       | 変更内容                                           |
| ---------- | ---------- | -------------------------------------------------- |
| 2.1        | 2025-12-07 | AWS 本番環境エンドポイント追加 (API Gateway HTTPS) |
| 2.0        | 2025-12-06 | 題材ベースの構造に変更、進捗管理 API 更新          |
| 1.0        | 2025-12-05 | 初版作成                                           |
