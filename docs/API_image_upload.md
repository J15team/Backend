# 画像アップロード機能 API リファレンス

J15 Backend の画像アップロード機能について説明します。セクションに対して画像をS3にアップロードし、取得したURLをDBに保存する機能を提供します。

## 基本情報

| 項目         | 内容                                                          |
| ------------ | ------------------------------------------------------------- |
| 本番 URL     | `https://zu9mkxoir4.execute-api.ap-northeast-1.amazonaws.com` |
| ローカル URL | `http://localhost:8080`                                       |
| 認証         | `Authorization: Bearer <accessToken>`（管理者権限必要）      |

---

## セクション画像アップロード

### セクション更新（画像アップロード含む）

既存のセクションに対して画像をアップロードし、セクション情報を更新します。

**エンドポイント**

- `PUT /api/subjects/{subjectId}/sections/{sectionId}` `ROLE_ADMIN`

**リクエスト**

- Content-Type: `multipart/form-data`
- 認証: 管理者権限が必要（`ROLE_ADMIN`）

**リクエストパラメータ**

| パラメータ   | 型            | 必須 | 説明                     |
| ------------ | ------------- | ---- | ------------------------ |
| subjectId    | long (path)    | ○    | 題材 ID                  |
| sectionId    | int (path)     | ○    | セクション ID（0~100）   |
| title        | string (form)  | ×    | セクションタイトル       |
| description  | string (form)  | ×    | セクション説明           |
| image        | file (form)    | ×    | アップロードする画像ファイル |

**画像ファイル制約**

- 許可される形式: JPEG, PNG, GIF, WebP
- 最大ファイルサイズ: 5MB
- ファイル名は自動生成（UUIDベース）

**リクエスト例（cURL）**

```bash
curl -X PUT \
  http://localhost:8080/api/subjects/1/sections/10 \
  -H "Authorization: Bearer <accessToken>" \
  -F "title=更新タイトル" \
  -F "description=更新説明" \
  -F "image=@/path/to/image.jpg"
```

**レスポンス**

**成功時 (200 OK)**

```json
{
  "subjectId": 1,
  "sectionId": 10,
  "title": "更新タイトル",
  "description": "更新説明",
  "imageUrl": "https://j15-backend-images.s3.ap-northeast-1.amazonaws.com/images/uuid.jpg"
}
```

**エラー**

- **400 Bad Request**: ファイル形式が不正、またはファイルサイズが大きすぎる
- **401 Unauthorized**: 認証トークンが無効または期限切れ
- **403 Forbidden**: 管理者権限がない
- **404 Not Found**: 題材またはセクションが存在しない
- **500 Internal Server Error**: S3アップロードに失敗

---

## セクション取得（画像URL確認）

セクション情報を取得すると、画像が登録されている場合は`imageUrl`フィールドにURLが含まれます。

**エンドポイント**

- `GET /api/subjects/{subjectId}/sections/{sectionId}`

**レスポンス例**

```json
{
  "subjectId": 1,
  "sectionId": 10,
  "title": "環境構築",
  "description": "開発環境のセットアップ",
  "imageUrl": "https://j15-backend-images.s3.ap-northeast-1.amazonaws.com/images/uuid.jpg"
}
```

画像が登録されていない場合、`imageUrl`は`null`になります。

---

## 使用例

### 1. セクションに画像を登録

```bash
# セクション更新時に画像をアップロード
curl -X PUT \
  http://localhost:8080/api/subjects/1/sections/10 \
  -H "Authorization: Bearer <accessToken>" \
  -F "image=@/path/to/image.jpg"
```

### 2. セクション情報と画像URLを取得

```bash
# セクション情報を取得（imageUrlが含まれる）
curl -X GET \
  http://localhost:8080/api/subjects/1/sections/10
```

### 3. 画像の有無を確認

レスポンスの`imageUrl`フィールドを確認することで、画像が登録されているかどうかを判断できます。

- `imageUrl`が`null`の場合: 画像未登録
- `imageUrl`がURL文字列の場合: 画像登録済み

---

## 実装フロー

1. フロントエンドから`PUT /api/subjects/{subjectId}/sections/{sectionId}`に画像ファイルを送信
2. バックエンドが画像ファイルを検証（形式、サイズ）
3. バックエンドがS3に画像をアップロード
4. S3から画像URLを取得
5. セクション情報をDBに更新（imageUrlを含む）
6. 更新されたセクション情報（imageUrl含む）をレスポンスとして返却

---

## 注意事項

- 画像アップロードには管理者権限（`ROLE_ADMIN`）が必要です
- 画像ファイルは自動的にUUIDベースのファイル名に変換されます
- 同じセクションに対して画像を再アップロードすると、既存の画像URLが上書きされます
- S3の認証情報は環境変数（`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`）で設定してください
- S3バケット名とリージョンは`application.yml`で設定可能です（デフォルト: `j15-backend-images`, `ap-northeast-1`）

---

## S3バケット設定

### バケット情報

- **バケット名**: `j15-backend-images`
- **リージョン**: `ap-northeast-1`
- **公開パス**: `images/*`（パブリック読み取り可能）

### バケット設定確認

以下のコマンドでバケットの設定を確認できます：

```bash
# バケットの存在確認
aws s3 ls | grep j15-backend-images

# バケットポリシー確認
aws s3api get-bucket-policy --bucket j15-backend-images

# CORS設定確認
aws s3api get-bucket-cors --bucket j15-backend-images
```

### バケット作成（必要な場合）

バケットが存在しない場合は、以下のコマンドで作成できます：

```bash
# バケット作成
aws s3 mb s3://j15-backend-images --region ap-northeast-1

# パブリックアクセスブロックを無効化（画像を公開するため）
aws s3api put-public-access-block \
  --bucket j15-backend-images \
  --public-access-block-configuration \
  "BlockPublicAcls=false,IgnorePublicAcls=false,BlockPublicPolicy=false,RestrictPublicBuckets=false"
```

### 環境変数設定

アプリケーション実行時に以下の環境変数を設定してください：

```bash
export AWS_ACCESS_KEY_ID=your-access-key-id
export AWS_SECRET_ACCESS_KEY=your-secret-access-key
export AWS_REGION=ap-northeast-1
export AWS_S3_BUCKET_NAME=j15-backend-images  # オプション（デフォルト値を使用する場合は不要）
```

