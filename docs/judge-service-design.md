# Judge Service 設計仕様

## 概要

Judge Service は、提出されたコードをサンドボックス環境で実行し、テストケースに対する判定結果を返す独立マイクロサービス。

## アーキテクチャ

```
┌─────────────────────────────────────────────────────────────┐
│                   Judge Service                              │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │   API Layer  │  │   Compiler   │  │  Result Comparator│   │
│  │  (HTTP/JSON) │  │   (GCC)      │  │                  │   │
│  └──────┬───────┘  └──────┬───────┘  └────────┬─────────┘   │
│         │                 │                    │             │
│         ▼                 ▼                    ▼             │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                    Executor (Sandbox)                   │ │
│  │  ┌─────────────────────────────────────────────────┐    │ │
│  │  │           Docker Container (isolate)            │    │ │
│  │  │  - CPU制限                                      │    │ │
│  │  │  - メモリ制限                                   │    │ │
│  │  │  - 時間制限                                     │    │ │
│  │  │  - ネットワーク隔離                             │    │ │
│  │  │  - ファイルシステム制限                         │    │ │
│  │  └─────────────────────────────────────────────────┘    │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 技術スタック

| コンポーネント | 技術選定         | 理由                         |
| -------------- | ---------------- | ---------------------------- |
| 言語           | Go               | 軽量、高速、並行処理に強い   |
| フレームワーク | Gin or Echo      | シンプルで高性能             |
| サンドボックス | isolate + Docker | 競技プログラミングで実績あり |
| コンパイラ     | GCC              | C 言語対応                   |

## API 仕様

### POST /api/judge

コードを判定する。

**Request:**

```json
{
  "code": "#include <stdio.h>\nint main() { printf(\"Hello\\n\"); return 0; }",
  "language": "c",
  "testCases": [
    { "input": "", "expected": "Hello\n" },
    { "input": "5", "expected": "25\n" }
  ],
  "timeLimit": 2000,
  "memoryLimit": 256
}
```

**Response (成功):**

```json
{
  "results": [
    {
      "index": 0,
      "verdict": "AC",
      "executionTime": 15,
      "memoryUsed": 1024,
      "actualOutput": "Hello\n"
    },
    {
      "index": 1,
      "verdict": "WA",
      "executionTime": 12,
      "memoryUsed": 1024,
      "actualOutput": "10\n"
    }
  ]
}
```

**Response (コンパイルエラー):**

```json
{
  "results": [
    {
      "index": 0,
      "verdict": "CE",
      "errorMessage": "main.c:1:10: fatal error: stdioo.h: No such file or directory"
    }
  ]
}
```

### GET /api/health

ヘルスチェック。

**Response:**

```json
{
  "status": "healthy",
  "supportedLanguages": ["c"]
}
```

## Verdict（判定結果）

| Verdict | 説明                                 |
| ------- | ------------------------------------ |
| AC      | Accepted - 正解                      |
| WA      | Wrong Answer - 不正解                |
| TLE     | Time Limit Exceeded - 時間超過       |
| MLE     | Memory Limit Exceeded - メモリ超過   |
| RE      | Runtime Error - 実行時エラー         |
| CE      | Compilation Error - コンパイルエラー |

## サンドボックス要件

### セキュリティ

1. **ネットワーク隔離**: 外部通信を完全に遮断
2. **ファイルシステム制限**: 読み書き可能な領域を最小限に
3. **プロセス制限**: fork bomb 対策
4. **システムコール制限**: seccomp で危険な syscall をブロック

### リソース制限

| リソース       | デフォルト | 最大  |
| -------------- | ---------- | ----- |
| CPU 時間       | 2 秒       | 10 秒 |
| メモリ         | 256MB      | 512MB |
| プロセス数     | 1          | 10    |
| ファイルサイズ | 10MB       | 50MB  |

## 実行フロー

```
1. リクエスト受信
   ↓
2. コードをファイルに書き出し
   ↓
3. コンパイル（C言語の場合: gcc -O2 -o main main.c）
   ├─ 失敗 → CE を返す
   ↓
4. 各テストケースに対して:
   a. Dockerコンテナ起動（isolate）
   b. 入力をstdinに渡して実行
   c. 時間・メモリを監視
   d. 出力を取得
   e. 期待値と比較
   ↓
5. 結果を集約して返す
```

## ディレクトリ構成（案）

```
judge-service/
├── cmd/
│   └── server/
│       └── main.go
├── internal/
│   ├── api/
│   │   ├── handler.go
│   │   └── router.go
│   ├── compiler/
│   │   ├── compiler.go
│   │   └── gcc.go
│   ├── executor/
│   │   ├── executor.go
│   │   ├── isolate.go
│   │   └── docker.go
│   ├── comparator/
│   │   └── comparator.go
│   └── config/
│       └── config.go
├── docker/
│   ├── Dockerfile
│   └── sandbox/
│       └── Dockerfile.sandbox
├── go.mod
├── go.sum
└── README.md
```

## 設定

```yaml
# config.yaml
server:
  port: 8081
  timeout: 30s

sandbox:
  image: "judge-sandbox:latest"
  network: "none"
  defaultTimeLimit: 2000 # ms
  defaultMemoryLimit: 256 # MB
  maxTimeLimit: 10000
  maxMemoryLimit: 512

compiler:
  c:
    command: "gcc"
    args: ["-O2", "-o", "main", "main.c"]
    timeout: 10s
```

## デプロイ

### Docker Compose（開発環境）

```yaml
version: "3.8"
services:
  judge-service:
    build: ./judge-service
    ports:
      - "8081:8081"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
    privileged: true # isolate用
```

### Kubernetes（本番環境）

- DaemonSet または Deployment で複数ノードに配置
- HPA（Horizontal Pod Autoscaler）で負荷に応じてスケール
- 専用ノードプールで隔離

## 今後の拡張

1. **言語追加**: Python, Java, C++, Rust など
2. **スペシャルジャッジ**: カスタム比較関数対応
3. **インタラクティブ問題**: 双方向通信対応
4. **並列実行**: 複数テストケースの同時実行
5. **キャッシュ**: 同一コードの再判定をスキップ

## メインバックエンドとの連携

```
Main Backend                          Judge Service
     │                                      │
     │  POST /api/judge                     │
     │  {code, language, testCases, ...}    │
     │ ─────────────────────────────────────▶│
     │                                      │
     │                                      │ コンパイル
     │                                      │ 実行
     │                                      │ 比較
     │                                      │
     │  {results: [{verdict, time, ...}]}   │
     │ ◀─────────────────────────────────────│
     │                                      │
     │  結果をDBに保存                       │
     │  スコア計算                           │
     ▼                                      ▼
```

## 参考

- [isolate](https://github.com/ioi/isolate) - IOI 公式サンドボックス
- [Judge0](https://github.com/judge0/judge0) - オープンソースオンラインジャッジ
- [AtCoder](https://atcoder.jp/) - 競技プログラミングサイト
