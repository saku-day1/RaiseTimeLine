現在の変更をコミットしてリモートにプッシュしてください。

## ルール（厳守）

- **main への直接プッシュは禁止**。現在のブランチが `main` の場合は作業を止めてユーザーに確認すること。
- コミットメッセージは**日本語**で記述する。
- ログファイル（`.backend.log`、`.frontend.log` 等）や `.claude/settings.local.json` はコミットに含めない。

## 手順

### ① 現状確認

```bash
git status
git diff --stat
git branch --show-current
```

### ② ステージング

- 変更ファイルを確認し、コミット対象のファイルのみを `git add` で追加する。
- ログファイル・ローカル設定ファイルは除外すること。

### ③ コミット

```bash
git commit -m "日本語のコミットメッセージ"
```

- 変更内容を端的に表す日本語メッセージを作成する。
- フッターに `Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>` を付ける。

### ④ プッシュ

```bash
git push -u origin <ブランチ名>
```

完了後、コミットハッシュとプッシュ先ブランチをユーザーに報告してください。
