# Pathly Learn - 機能一覧

## 概要

Pathly Learn は、プログラミング学習を支援する Web アプリケーションです。
学習コンテンツの閲覧から、実際にコードを書いて提出・自動採点まで、一貫した学習体験を提供します。

---

## 🎓 学習コンテンツ機能

### 題材（Subject）管理

学習コンテンツを「題材」単位で管理します。

- **題材一覧表示**: 利用可能な学習コンテンツを一覧で確認
- **題材詳細表示**: 各題材の説明、セクション構成を確認
- **進捗管理**: 各セクションの完了状態を記録・表示

### セクション管理

各題材は複数のセクションで構成されます。

- **説明セクション**: 概念や文法の解説
- **セクション完了マーク**: 学習完了を記録

---

## 💻 課題実行システム

### 課題題材（Assignment Subject）

プログラミング課題を含む学習コンテンツです。

- **課題題材一覧**: `GET /api/assignments`
- **課題題材詳細**: `GET /api/assignments/{id}`

### 課題セクション（Assignment Section）

各課題題材は複数のセクションで構成されます。

| セクションタイプ | 説明                         |
| ---------------- | ---------------------------- |
| 説明のみ         | 概念や文法の解説（課題なし） |
| 課題あり         | コード提出・自動採点あり     |

- **セクション一覧**: `GET /api/assignments/{id}/sections`
- **セクション詳細**: `GET /api/assignments/{id}/sections/{sectionId}`

### コード提出・自動採点

課題ありセクションでは、コードを提出して自動採点を受けられます。

```
POST /api/assignments/{id}/sections/{sectionId}/submissions
{
  "code": "ソースコード",
  "language": "c"
}
```

**採点結果**:

- ✅ 正解（AC）: テストケース通過
- ❌ 不正解（WA）: 出力が期待値と異なる
- ⏱️ 時間超過（TLE）: 制限時間オーバー
- 💥 実行時エラー（RE）: セグフォなど
- 🔧 コンパイルエラー（CE）: 構文エラー

**部分点システム**:

- 通過テストケース数に応じて 0〜100 点
- 例: 3/5 テストケース通過 → 60 点

### 提出履歴

過去の提出を確認できます。

- **履歴一覧**: `GET /api/assignments/{id}/sections/{sectionId}/submissions`
- **提出詳細**: `GET /api/assignments/{id}/sections/{sectionId}/submissions/{submissionId}`

### 進捗確認

課題の進捗状況を確認できます。

```
GET /api/assignments/{id}/progress

{
  "sections": [
    {"sectionId": 1, "title": "Hello World", "bestScore": 100, "isCleared": true},
    {"sectionId": 3, "title": "足し算", "bestScore": 60, "isCleared": false}
  ],
  "totalSections": 3,
  "clearedSections": 1,
  "isSubjectCleared": false,
  "progressPercent": 33
}
```

- **セクションクリア条件**: 100 点を取得
- **題材クリア条件**: 全課題ありセクションをクリア

### コードプレビュー（実行テスト）

提出前にコードを実行して結果を確認できます。

```
POST /api/code/preview
{
  "code": "ソースコード",
  "language": "c",
  "input": "5 3\n"
}

{
  "output": "8\n",
  "executionTime": 15,
  "status": "SUCCESS"
}
```

---

## 👤 ユーザー管理

### 認証

- **メール/パスワード認証**: 従来型のログイン
- **Google OAuth**: Google アカウントでログイン
- **GitHub OAuth**: GitHub アカウントでログイン

### ユーザー情報

- **プロフィール取得**: `GET /api/users/me`
- **プロフィール更新**: `PUT /api/users/me`
- **アバター画像アップロード**: S3 に保存

### ロール

| ロール | 権限                         |
| ------ | ---------------------------- |
| USER   | 学習コンテンツ閲覧、課題提出 |
| ADMIN  | 上記 + コンテンツ管理        |

---

## 🏆 ランキング機能

### 題材ランキング

- **閲覧数ランキング**: 人気の題材を表示
- **評価ランキング**: 高評価の題材を表示

---

## 🔧 管理者機能

ADMIN ロールを持つユーザーは、学習コンテンツの作成・編集・削除が可能です。

### 学習題材管理（ADMIN 権限必須）

一般的な学習コンテンツ（課題なし）の管理。

- **題材作成**: `POST /api/subjects`
- **題材更新**: `PUT /api/subjects/{id}`
- **題材削除**: `DELETE /api/subjects/{id}`
- **セクション管理**: 各題材内のセクションを追加・編集・削除

### 課題題材管理（ADMIN 権限必須）

プログラミング課題を含む学習コンテンツの管理。

- **課題題材作成**: `POST /api/assignments`
- **課題題材更新**: `PUT /api/assignments/{id}`
- **課題題材削除**: `DELETE /api/assignments/{id}`

### 課題セクション管理（ADMIN 権限必須）

課題題材内のセクション（説明・課題）の管理。

- **セクション作成**: `POST /api/assignments/{id}/sections`
- **セクション更新**: `PUT /api/assignments/{id}/sections/{sectionId}`
- **セクション削除**: `DELETE /api/assignments/{id}/sections/{sectionId}`
- **テストケース設定**: 課題ありセクションには入出力テストケースを設定

### 管理者になるには

システム管理者に依頼して ADMIN ロールを付与してもらう必要があります。

---

## 🔒 セキュリティ

- **JWT 認証**: アクセストークン + リフレッシュトークン
- **レート制限**: API 呼び出し回数制限
- **CORS 設定**: 許可されたオリジンのみアクセス可能
- **サンドボックス実行**: 提出コードは隔離環境で実行

---

## 📊 対応言語

現在対応している言語:

- **C 言語**

Coming soon...

- Python
- Java
- C++

---

## 🏗️ システム構成

| コンポーネント | 技術              | 説明                     |
| -------------- | ----------------- | ------------------------ |
| Frontend       | Next.js (Vercel)  | ユーザーインターフェース |
| API Gateway    | AWS API Gateway   | HTTPS 化、ルーティング   |
| Backend        | Spring Boot (ECS) | メイン API               |
| Database       | PostgreSQL (RDS)  | データ永続化             |
| Storage        | S3                | 画像ファイル保存         |
| Judge Service  | Go (ECS)          | コード実行・採点         |

---

## 📝 サンプル課題

現在登録されているサンプル課題:

**C 言語基礎**

1. Hello World（課題あり）
2. 変数とは（説明のみ）
3. 足し算プログラム（課題あり）
4. 条件分岐（説明のみ）
5. 偶数・奇数判定（課題あり）
