# J15 Backend API クイックリファレンス

最短でエンドポイントを確認できるよう、必須情報だけをまとめた簡易仕様です。詳細なバリデーションやエラーパターンは `docs/API_v2.md` を参照してください。

## ベース URL

| 環境 | URL |
| --- | --- |
| 本番 | `https://zu9mkxoir4.execute-api.ap-northeast-1.amazonaws.com` |
| ローカル | `http://localhost:8080` |

## 認証

1. `POST /api/auth/signin` で `accessToken` と `refreshToken` を取得。
2. 認証が必要な API では `Authorization: Bearer <accessToken>` を付与。
3. アクセストークン失効時は `POST /api/auth/refresh` に `refreshToken` を渡し再発行。
4. レートリミットと JWT フィルタがリクエスト毎に適用されます。

---

## エンドポイント一覧

### 認証 API

| メソッド | パス | 認証 | 説明 | リクエスト例 | 成功レスポンス |
| --- | --- | --- | --- | --- | --- |
| POST | `/api/auth/signup` | 不要 | 新規ユーザー登録 | `{"username":"alice","email":"alice@example.com","password":"pass1234"}` | 201 / `LoginResponse`（accessToken, refreshToken, user情報） |
| POST | `/api/auth/signin` | 不要 | ログイン | `{"email":"alice@example.com","password":"pass1234"}` | 200 / `LoginResponse` |
| POST | `/api/auth/refresh` | 不要 | リフレッシュトークンでアクセストークン再発行 | `{"refreshToken":"<token>"}` | 200 / `{"accessToken":"...","refreshToken":"..."}` |

### 題材 / セクション API

| メソッド | パス | 認証 | 説明 |
| --- | --- | --- | --- |
| GET | `/api/subjects` | 不要 | 題材一覧取得 |
| GET | `/api/subjects/{subjectId}` | 不要 | 題材詳細取得 |
| POST | `/api/subjects` | `ROLE_ADMIN` | 題材を新規作成 |
| PUT | `/api/subjects/{subjectId}` | `ROLE_ADMIN` | 題材を更新 |
| DELETE | `/api/subjects/{subjectId}` | `ROLE_ADMIN` | 題材を削除 |
| GET | `/api/subjects/{subjectId}/sections` | 不要 | セクション一覧取得 |
| GET | `/api/subjects/{subjectId}/sections/{sectionId}` | 不要 | セクション詳細取得 |

### 進捗 API（要ログイン）

| メソッド | パス | 説明 | 主なリクエスト項目 |
| --- | --- | --- | --- |
| GET | `/api/progress/subjects/{subjectId}` | 指定題材のユーザー進捗を取得 | なし（認証情報からユーザー判定） |
| GET | `/api/progress/subjects/{subjectId}/sections/{sectionId}` | セクションを完了済みか判定 | なし |
| POST | `/api/progress/subjects/{subjectId}/sections` | セクションを完了扱いにする | `{"sectionId":10}` |
| DELETE | `/api/progress/subjects/{subjectId}/sections/{sectionId}` | 完了状態を取り消す | パスパラメータのみ |

### 管理者 API

| メソッド | パス | 認証 | 説明 |
| --- | --- | --- | --- |
| POST | `/api/admin/users` | `X-Admin-Key` ヘッダー | 管理者ユーザー作成（API キーは `ADMIN_API_KEY` 環境変数） |

---

## 共通レスポンス

- すべて JSON を返却。
- 成功時: 200 / 201 / 204
- 代表的なエラー:
  - 400: バリデーション、ドメインルール違反
  - 401: JWT 不正または欠如
  - 403: 権限不足
  - 404: リソースなし
  - 409: 重複

必要最低限の情報だけをまとめているため、パラメータの詳細やドメイン説明が必要な場合は `docs/API_v2.md` を参照してください。
