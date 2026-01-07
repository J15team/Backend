# 技術スタック詳細

## 言語とフレームワーク
- **Kotlin 1.9.21** + Java 17
- **Spring Boot 3.2.0**
  - spring-boot-starter-web
  - spring-boot-starter-data-jpa
  - spring-boot-starter-security
  - spring-boot-starter-validation
  - spring-boot-starter-actuator

## データベースとマイグレーション
- **PostgreSQL 16** (本番: AWS RDS)
- **Flyway** (マイグレーション管理)
- **H2** (テスト用インメモリDB)

## セキュリティとレート制限
- **Spring Security** (認証・認可)
- **JWT**: jjwt-api/impl/jackson 0.12.3
- **Bucket4j 8.7.0** (レート制限実装)
- **Caffeine 3.1.8** (キャッシュバックエンド)

## クラウドサービス
- **AWS S3 SDK 2.20.26** (画像ストレージ)
- **AWS RDS** (本番データベース)
- **AWS ECS** (コンテナ実行環境)

## 開発ツール
- **Gradle 8.5** (ビルドツール)
- **Docker & Docker Compose** (開発環境)
- **GitHub Actions** (CI/CD)
