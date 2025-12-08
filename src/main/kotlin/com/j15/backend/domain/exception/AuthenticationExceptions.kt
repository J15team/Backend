package com.j15.backend.domain.exception

/**
 * 認証関連の例外
 */
sealed class AuthenticationException(message: String) : RuntimeException(message)

/**
 * ユーザーが見つからない場合の例外
 */
class UserNotFoundException(message: String = "ユーザーが見つかりません") : AuthenticationException(message)

/**
 * 認証情報が不正な場合の例外
 */
class InvalidCredentialsException(message: String = "メールアドレスまたはパスワードが正しくありません") : AuthenticationException(message)

/**
 * メールアドレスが既に登録されている場合の例外
 */
class DuplicateEmailException(message: String = "このメールアドレスは既に登録されています") : AuthenticationException(message)

/**
 * ユーザー名が既に使用されている場合の例外
 */
class DuplicateUsernameException(message: String = "このユーザー名は既に使用されています") : AuthenticationException(message)

/**
 * トークンが不正な場合の例外
 */
class InvalidTokenException(message: String = "トークンが不正です") : AuthenticationException(message)
