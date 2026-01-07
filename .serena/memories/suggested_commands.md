# 推奨コマンド一覧

## ビルドとテスト
```bash
# プロジェクトビルド
./gradlew build

# クリーンビルド
./gradlew clean build

# テスト実行
./gradlew test
```

## Docker環境管理
```bash
# アプリケーション起動（バックグラウンド）
docker-compose up -d

# ログ確認
docker-compose logs -f

# コンテナ停止
docker-compose down

# コンテナ完全削除（ボリューム含む）
docker-compose down -v

# 再ビルドして起動
docker-compose up -d --build
```

## ローカル開発サーバー
```bash
# Spring Boot アプリケーション起動
./gradlew bootRun

# 管理者権限で起動（管理者API有効化）
ADMIN_API_KEY=test-local-api-key-12345 ./gradlew bootRun
```

## AWS開発環境管理
```bash
# AWS開発環境起動（RDS + ECS）
./scripts/aws-dev.sh start

# AWS開発環境停止
./scripts/aws-dev.sh stop

# 状態確認
./scripts/aws-dev.sh status
```

## API動作確認
```bash
# ヘルスチェック
curl http://localhost:8080/api/health

# 題材一覧取得
curl http://localhost:8080/api/subjects

# セクション一覧取得
curl http://localhost:8080/api/subjects/1/sections

# サインアップ
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'

# サインイン
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'

# 進捗取得（要認証）
curl -H "Authorization: Bearer <ACCESS_TOKEN>" \
  http://localhost:8080/api/progress/subjects/1
```

## Darwin (macOS) 固有コマンド
```bash
# プロセス確認
lsof -i :8080

# プロセス終了
lsof -ti :8080 | xargs kill

# ファイル検索
find . -name "*.kt"

# コード検索
grep -r "pattern" src/

# ディレクトリツリー表示（brewでインストール必要）
tree -L 3 src/
```

## Git ワークフロー
```bash
# 現在のブランチ確認
git branch

# feature ブランチ作成
git checkout -b feature/your-feature-name

# ステージングとコミット
git add .
git commit -m "feat: your commit message"

# プッシュ
git push origin feature/your-feature-name
```

## タスク完了時の推奨フロー
1. `./gradlew build` - ビルド確認
2. `docker-compose up -d` - 動作確認
3. `curl http://localhost:8080/api/health` - ヘルスチェック
4. 必要に応じてE2Eテスト実行
