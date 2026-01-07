# コードスタイルとコンベンション

## Kotlin コーディング規約
- **命名規則**:
  - クラス名: PascalCase (例: `UserRepository`, `SecurityConfig`)
  - 関数名/変数名: camelCase (例: `getUserProgress`, `userId`)
  - 定数: UPPER_SNAKE_CASE
  - パッケージ名: lowercase (例: `com.j15.backend.domain.model`)

- **data class の活用**: ドメインモデルは data class で定義
  ```kotlin
  data class User(
      val userId: UserId,
      val username: Username,
      val email: Email,
      val passwordHash: PasswordHash,
      val role: UserRole = UserRole.ROLE_USER,
      val createdAt: Instant = Instant.now()
  )
  ```

- **値オブジェクト**: 型安全性のため Value Object パターンを使用
  - `UserId`, `Username`, `Email`, `SubjectId`, `SectionId` など

- **コメント**: 日本語コメントを使用
  ```kotlin
  // ユーザーエンティティ（ドメイン層）永続化の詳細から独立したドメインモデル
  ```

## アーキテクチャパターン
- **オニオンアーキテクチャ**の厳格な適用
  - Domain層: 外部依存なし、純粋なビジネスロジック
  - Application層: ユースケース実装、ドメインサービス利用
  - Infrastructure層: 永続化、外部API連携
  - Presentation層: HTTP エンドポイント、DTO変換

- **依存性の方向**: 外側の層が内側の層に依存（内側は外側を知らない）

## Spring アノテーション
- `@Configuration`, `@EnableWebSecurity`: 設定クラス
- `@RestController`, `@RequestMapping`: コントローラー
- `@Service`, `@Repository`: サービス・リポジトリ層
- `@PreAuthorize`: メソッドレベルの認可
- `@Bean`: DIコンテナへの登録

## セキュリティ実装パターン
- JWT認証フィルター: `JwtAuthenticationFilter`
- レート制限フィルター: `RateLimitFilter` 
- フィルターチェーン順序: RateLimit → JWT → UsernamePassword
