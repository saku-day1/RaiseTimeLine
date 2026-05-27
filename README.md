# RaiseTimeLine

Twitter/X 風のタイムライン形式 SNS アプリケーション。AIコース課題として、要件定義・設計・実装の一連のプロセスを体験する目的で開発。複数ユーザーが投稿・コメント・いいね・フォローを行えるシンプルな SNS。

---

## 機能

- **認証** — メールアドレス＋パスワードで登録・ログイン（JWT 認証）
- **タイムライン** — 「ホーム（フォロー中）」と「すべて」タブを切り替えて閲覧
- **投稿** — テキスト（最大140文字）＋画像（任意）で投稿・削除
- **コメント** — 他ユーザーの投稿にコメントを投稿・削除。コメント数を可視化
- **いいね** — 投稿にいいねを付与・取り消し（1ユーザー1投稿1回）。いいね数を可視化
- **フォロー** — ユーザー検索（部分一致）からフォロー・アンフォロー
- **プロフィール** — ユーザー名・自己紹介文（最大160文字）・プロフィール画像を編集
- **画像アップロード** — 投稿画像・プロフィール画像を AWS S3 に保存

---

## 技術スタック

| 領域 | 技術 | バージョン |
|------|------|-----------|
| フロントエンド | React + TypeScript | React 19.2.6 / TypeScript 6.0.x |
| ルーティング | React Router | 7.x |
| HTTP クライアント | Axios | 1.x |
| フロントビルド | Vite | 8.0.x |
| バックエンド | Java / Spring Boot | Java 21 / Spring Boot 3.5.0 |
| 認証 | Spring Security（JWT） | （Spring Boot 管理） |
| ORM | Spring Data JPA | （Spring Boot 管理） |
| DB | PostgreSQL | 17 |
| バックビルド | Gradle | 8.14.4 |
| 画像ストレージ | AWS S3 | - |

---

## セットアップ・起動手順

### 前提条件

- Docker Desktop がインストールされていること
- JDK 21 がインストールされていること
- Node.js がインストールされていること

### 起動順序（厳守）

```
① DB（Docker） → ② バックエンド → ③ フロントエンド
```

#### 1. DB 起動

```bash
docker compose up -d
```

#### 2. バックエンド起動

```bash
cd backend
./gradlew bootRun
```

#### 3. フロントエンド起動

```bash
cd frontend
npm install
npm run dev
```

### アクセス先

| サービス | URL |
|---------|-----|
| フロントエンド | http://localhost:5173 |
| バックエンド API | http://localhost:8080 |
| DB | localhost:5432 |

---

## データモデル

```
users
├── id                : 主キー
├── email             : メールアドレス（一意）
├── password_hash     : bcrypt ハッシュ
├── username          : ユーザー名（最大50文字）
├── bio               : 自己紹介文（任意・最大160文字）
├── profile_image_url : プロフィール画像 URL（S3）
├── created_at        : 作成日時
└── updated_at        : 更新日時

posts
├── id         : 主キー
├── user_id    : 投稿者（FK → users）
├── content    : 本文（最大140文字）
├── image_url  : 添付画像 URL（S3・任意）
├── created_at : 投稿日時
└── updated_at : 更新日時

comments
├── id         : 主キー
├── post_id    : 対象投稿（FK → posts）
├── user_id    : コメント者（FK → users）
├── content    : 本文（最大200文字）
└── created_at : コメント日時

likes
├── id         : 主キー
├── post_id    : 対象投稿（FK → posts）
├── user_id    : いいねしたユーザー（FK → users）
└── created_at : いいね日時
※ (post_id, user_id) に UNIQUE 制約

follows
├── id           : 主キー
├── follower_id  : フォローするユーザー（FK → users）
├── following_id : フォローされるユーザー（FK → users）
└── created_at   : フォロー日時
※ (follower_id, following_id) に UNIQUE 制約・自己フォロー禁止
```

---

## インフラ構成（AWS）

AWS 上に ALB・EC2・RDS・S3 を使った本番相当の環境を構築する。

```
インターネット
    ↓ HTTPS (443)
ALB（Application Load Balancer）
    ↓ HTTP (80)
EC2 t3.small（Nginx + Spring Boot）
    ↓                          ↓
RDS db.t4g.micro          S3 Bucket
（PostgreSQL 17）          （画像ファイル）
```

詳細は [技術仕様](docs/07_技術仕様.md) を参照。

---

## カスタムコマンド

Claude Code で以下のスラッシュコマンドが使用できる。

| コマンド | 用途 |
|---------|------|
| `/start-dev` | DB・バックエンド・フロントエンドを起動 |
| `/commit` | 変更をコミットしてリモートにプッシュ |
| `/pr` | プルリクエストを作成 |
| `/review` | 実装が要件定義・設計と合っているか確認 |
| `/code-review` | コード品質・セキュリティ・保守性の総合レビュー |
| `/quality-check` | ESLint・Checkstyle を実行して品質チェック |

---

## ドキュメント

詳細は [要件定義書.md](要件定義書.md) をハブとして各ドキュメントを参照。

| # | ドキュメント | 内容 |
|---|-------------|------|
| 1 | [機能要件](docs/01_機能要件.md) | 認証・タイムライン・投稿・コメント・いいね・フォロー・検索・画像投稿 |
| 2 | [非機能要件](docs/02_非機能要件.md) | パフォーマンス・セキュリティ・可用性・保守性 |
| 3 | [バリデーション](docs/03_バリデーション.md) | 各入力項目のバリデーション条件 |
| 4 | [ユースケース](docs/04_ユースケース.md) | UC01〜UC08 の操作フロー |
| 5 | [画面仕様](docs/05_画面仕様.md) | レイアウト・画面遷移図・各画面の UI 要素 |
| 6 | [データ設計](docs/06_データ設計.md) | ER 図・テーブル定義・DDL・クエリ例 |
| 7 | [技術仕様](docs/07_技術仕様.md) | ファイル構成・使用技術・AWS インフラ構成図 |
| - | [機能定義書一覧](docs/features/) | F01〜F07 機能単位の詳細定義 |
