# ランキング機能 変更点一覧

## 新規追加ファイル

### データベース
- `src/main/resources/db/migration/V13__add_ranking_support.sql`
  - `subject_views` テーブル（題材閲覧記録）
  - `tag_views` テーブル（タグ閲覧記録）

### ドメイン層
- `src/main/kotlin/com/j15/backend/domain/model/ranking/SubjectView.kt`
- `src/main/kotlin/com/j15/backend/domain/model/ranking/TagView.kt`
- `src/main/kotlin/com/j15/backend/domain/model/ranking/SubjectRanking.kt`
- `src/main/kotlin/com/j15/backend/domain/model/ranking/TagRanking.kt`
- `src/main/kotlin/com/j15/backend/domain/repository/SubjectViewRepository.kt`
- `src/main/kotlin/com/j15/backend/domain/repository/TagViewRepository.kt`

### インフラストラクチャ層
- `src/main/kotlin/com/j15/backend/infrastructure/entity/SubjectViewJpaEntity.kt`
- `src/main/kotlin/com/j15/backend/infrastructure/entity/TagViewJpaEntity.kt`
- `src/main/kotlin/com/j15/backend/infrastructure/repository/jpa/JpaSubjectViewRepository.kt`
- `src/main/kotlin/com/j15/backend/infrastructure/repository/jpa/JpaTagViewRepository.kt`
- `src/main/kotlin/com/j15/backend/infrastructure/repository/SubjectViewRepositoryImpl.kt`
- `src/main/kotlin/com/j15/backend/infrastructure/repository/TagViewRepositoryImpl.kt`

### アプリケーション層
- `src/main/kotlin/com/j15/backend/application/usecase/ranking/RankingUseCase.kt`

### プレゼンテーション層
- `src/main/kotlin/com/j15/backend/presentation/dto/ranking/RankingResponse.kt`
  - `SubjectRankingResponse`
  - `TagRankingResponse`
  - `ViewRecordResponse`
- `src/main/kotlin/com/j15/backend/presentation/controller/ranking/ViewRecordController.kt`
- `src/main/kotlin/com/j15/backend/presentation/controller/ranking/RankingController.kt`

### ドキュメント
- `docs/ranking-api.md` - API仕様書
- `docs/ranking-feature-changelog.md` - 変更点一覧（このファイル）

## 変更ファイル

### セキュリティ設定
- `src/main/kotlin/com/j15/backend/infrastructure/security/SecurityConfig.kt`
  - ランキング取得API (`GET /api/rankings/**`) を認証不要に設定
  - 閲覧記録API (`POST /api/views/**`) を認証必須に設定

## データベーススキーマ

### subject_views テーブル

```sql
CREATE TABLE subject_views (
    subject_id BIGINT NOT NULL,
    user_id UUID NOT NULL,
    viewed_at TIMESTAMP NOT NULL,
    PRIMARY KEY (subject_id, user_id),
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
```

### tag_views テーブル

```sql
CREATE TABLE tag_views (
    tag_id BIGINT NOT NULL,
    user_id UUID NOT NULL,
    viewed_at TIMESTAMP NOT NULL,
    PRIMARY KEY (tag_id, user_id),
    FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
```

## アーキテクチャ

オニオンアーキテクチャに準拠:

```
presentation (Controller, DTO)
    ↓
application (UseCase)
    ↓
domain (Model, Repository Interface)
    ↓
infrastructure (JPA Entity, Repository Impl)
```

## 新規エンドポイント

| メソッド | パス | 認証 | 説明 |
|---------|------|------|------|
| POST | `/api/views/subjects/{subjectId}` | 必須 | 題材の閲覧を記録 |
| POST | `/api/views/tags/{tagId}` | 必須 | タグの閲覧を記録 |
| GET | `/api/rankings/subjects?limit=10` | 不要 | 題材ランキングを取得 |
| GET | `/api/rankings/tags?limit=10` | 不要 | タグランキングを取得 |

## 特徴

1. **同一アカウント1回カウント**: `(subject_id, user_id)` / `(tag_id, user_id)` の複合主キーにより、同じユーザーからの重複アクセスはカウントされない

2. **認証ユーザーのみカウント**: 閲覧記録は認証必須、ランキング取得は公開

3. **カスケード削除**: 題材/タグが削除されると、関連する閲覧記録も自動削除
