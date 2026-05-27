フロントエンド（React）とバックエンド（Spring Boot + PostgreSQL）を起動してください。

## 使用ポート（変更禁止）

| サービス | ポート |
|---------|--------|
| DB（PostgreSQL / Docker） | 5432 |
| バックエンド（Spring Boot） | 8080 |
| フロントエンド（Vite + React） | 5173 |

## 起動順序（厳守）

```
① DB（Docker）→ ② バックエンド → ③ フロントエンド
```

DB が起動していない状態でバックエンドを起動すると PostgreSQL 接続エラーで落ちる。
必ずこの順序で起動すること。

## 起動前のポート競合チェック（必須）

起動前に以下を実行し、競合プロセスがあれば停止してから起動する。

```bash
# 競合確認（Windows）
netstat -ano | findstr ":8080 "
netstat -ano | findstr ":5173 "

# 競合PIDを停止（Windows）
taskkill /PID <PID> /F

# Mac/Linux
lsof -ti:8080 | xargs kill -9
lsof -ti:5173 | xargs kill -9
```

## 起動手順

### ① DB（PostgreSQL）

```bash
docker compose up -d
```

- プロジェクトルートで実行
- `backend/` または `docker-compose.yml` がない場合はスキップし「DBはまだ未実装です」と伝える

### ② バックエンド（Spring Boot）

DB の起動を確認してから実行する。

```bash
cd backend && ./gradlew bootRun
```

- URL: http://localhost:8080
- 起動完了の目安: `Started RaiseTimeLineApplication` のログ
- `backend/` がない場合はスキップし「バックエンドはまだ未実装です」と伝える

### ③ フロントエンド（Vite + React）

依存パッケージが未インストールの場合（`frontend/node_modules/` がない）は先に実行する。

```bash
cd frontend && npm install
```

```bash
cd frontend && npm run dev
```

- URL: http://localhost:5173（使用中なら Vite が別ポートを自動選択）
- `frontend/` がない場合はスキップし「フロントエンドはまだ未実装です」と伝える

## ポート競合ルール（厳守）

- ポートが競合した場合は **既存プロセスを必ず停止** してから起動する
- 別のポートで代替起動することは禁止（フロント↔バックエンド間の URL 設定が壊れるため）
- 必ずデフォルトポート（5432 / 8080 / 5173）で起動すること

すべて起動し、完了を確認してからユーザーに URL を明示して報告してください。
