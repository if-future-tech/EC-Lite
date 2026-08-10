# EC-Lite

最小構成のECサイト（Java / Servlet・JSP、フレームワーク非依存）。Java案件獲得を目的とした教育・ポートフォリオ用プロジェクトですが、設計判断・業務ルールは実務基準で検討しています。

Spring Boot 等のフレームワークに頼らず素の Servlet/JSP で構築することで、フレームワークが裏側で肩代わりしている責務（DI、ライフサイクル管理、View解決等）を自分の手で設計・実装しています。

---

## 特徴

- ドメインモデル設計とRepositoryパターンによる永続化層の抽象化（InMemory ⇄ JDBC を無停止で差し替え可能）
- コンソール版（デバッグ用CLI）とWeb版が同一のService層を共有する二重エントリーポイント構成
- Firebase Authentication（Web版）とInMemory認証（コンソール版）の二重認証構成
- 業務ロジック：再注文制御（24時間×数量ルール）、在庫連動キャンセル、返品申請の導線
- JUnit 5による自動テスト（35件）

## 技術スタック

| 領域 | 技術 |
|---|---|
| 言語 | Java（JDK 25で記述、`--release 24`でコンパイル） |
| Web | Servlet / JSP（JSTL 3.0）、Apache Tomcat 10.1 |
| 永続化 | PostgreSQL（[Neon](https://neon.tech)）+ JDBC（HikariCP） |
| 認証 | Firebase Authentication（Web版）／InMemory（コンソール版） |
| デプロイ | Google Cloud Run、Docker |
| テスト | JUnit 5（JUnit Platform Console Standalone） |

## クイックスタート

### 必要環境

- JDK 25
- Apache Tomcat 10.1系
- （任意）Google Cloud SDK（Cloud Runデプロイ用）

環境構築で詰まる場合は [`docs/開発環境構築ガイド_初心者向け.md`](docs/開発環境構築ガイド_初心者向け.md) を参照してください。

### ローカル実行（Web版）

```bash
javac -cp "lib/*" --release 24 -d webapp/WEB-INF/classes $(find src -name "*.java")
```

Tomcat起動後、`http://localhost:8888/ec-lite/products` にアクセス。

### ローカル実行（コンソール版・デバッグ用）

```bash
javac -cp "lib/*" --release 24 -d console-classes $(find src -name "*.java")
java -cp console-classes ec.app.Main
```

InMemory認証のみで動作し、ネットワーク接続不要です。初回ログイン：`alice` / `password`。

### テスト実行

```bash
javac -cp "lib/*" --release 24 -d test-classes $(find src -name "*.java") $(find test -name "*.java")
java -jar lib/junit-platform-console-standalone-1.10.0.jar execute --class-path test-classes --scan-class-path --details=tree
```

## プロジェクト構成

```
src/ec/
├─ model/       ドメインモデル
├─ repository/  Repositoryインターフェース + InMemory/JDBC実装
├─ service/     業務ロジック
├─ app/         コンソール版エントリーポイント
└─ servlet/     Web版エントリーポイント
webapp/         JSP・静的ファイル
test/           JUnitテスト
```

## ドキュメント

詳細な仕様・開発の経緯・トラブルシューティングは `docs/` 配下を参照してください。

- [完成版仕様書 README](docs/EC-Lite_完成版仕様書_README_v2.md) — 詳細仕様
- [開発トレース総合ガイド](docs/EC-Lite_開発トレース_総合ガイド.md) — ゼロから完成までの再現手順
- [エラー対処マニュアル](docs/EC-Lite_エラー対処マニュアル.md) — トラブルシューティング逆引き集
- [開発環境構築ガイド](docs/開発環境構築ガイド_初心者向け.md) — OS別セットアップ手順

## ライセンス

このプロジェクトは **GNU Affero General Public License v3.0 (AGPL-3.0)** の下で公開しています。詳細は [`LICENSE`](LICENSE) ファイル、または [gnu.org の公式条文](https://www.gnu.org/licenses/agpl-3.0.html) を参照してください。
