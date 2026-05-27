現在のブランチのプルリクエストを作成してください。

## ルール（厳守）

- PR タイトルは `[#イシュー番号] 日本語の説明` の形式にする。
  - 例: `[#12] フォロー機能を実装する`
- 本文に `Closes #番号` を書いてイシューと紐付ける。
- ベースブランチは `main`。
- **現在のブランチが `main` の場合は作業を止めてユーザーに確認すること。**

## 手順

### ① 現状確認

```bash
git branch --show-current
git log main..HEAD --oneline
git diff main...HEAD --stat
```

ブランチ名からイシュー番号を読み取る（例: `feature/issue-12-follow` → `#12`）。

### ② PR 作成

```bash
gh pr create \
  --title "[#番号] 日本語タイトル" \
  --body "..."
```

本文に含めること:

- **概要**: 何を実装・修正したか（1〜3行）
- **変更内容**: 箇条書きで変更ファイルと内容
- **テスト確認項目**: チェックリスト形式
- `Closes #番号`
- `🤖 Generated with [Claude Code](https://claude.com/claude-code)`

完了後、PR の URL をユーザーに報告してください。
