# peacemind 練習用プロジェクト

Spring Boot + Heroku + Vue.js の学習用リポジトリ。

---

## 2026-04-04 今日やったこと

### 1. Spring Boot + Heroku デプロイ
- `Procfile`・`system.properties`・`application.properties` を作成
- `HelloController.java` にエンドポイントを追加
- `git push heroku master` で自動デプロイ完了

### 2. Heroku Postgres（PostgreSQL）を追加
- `heroku addons:create heroku-postgresql:essential-0` でDBを追加
- `Message.java`（Entity）・`MessageRepository.java`（JpaRepository）を作成
- `pom.xml` に `spring-boot-starter-data-jpa` と `postgresql` を追加
- GET `/api/messages`・POST `/api/messages` エンドポイントを実装

### 3. AWS移行の設計メモを作成
- `docs/aws-migration.md` に構成提案・Terraformを記載
- `docs/aws-migration.drawio` にアーキテクチャ図を作成

### 4. 学んだこと
- HerokuのDynoはLinuxコンテナ（Dockerに近い）
- Heroku PostgresはAWS RDS上で動いている
- Aurora Serverless v2はPostgreSQL互換 → コード変更なしで移行可能
- JpaRepositoryを使うとSQLを書かずにDB操作できる
- peacemindはHeroku→AWS移行を検討中

### 次回やること
- Dockerfileを作成してAWS移行の準備を進める
- ECS Fargate + Aurora Serverless v2 へのデプロイを試す
- Vue.jsの `watch` / ライフサイクル（`onMounted`）を学ぶ

---

## Jira × GitHub 連携メモ

### 基本的な使い方
JiraのissueIDをコミットメッセージに含めると自動でリンクされる。

```bash
git commit -m "PM-123 名前登録機能を追加"
#                ↑ JiraのissueID（プロジェクトキー-番号）
```

### 連携でできること
| 操作 | 自動で起きること |
|------|----------------|
| コミットメッセージにissueIDを書く | JiraのissueにGitHubのコミットがリンクされる |
| PRをマージする | Jiraのステータスが自動更新される |
| ブランチ名にissueIDを含める | Jiraのissueにブランチがリンクされる |

### ブランチ名の例
```bash
git checkout -b feature/PM-123-名前登録機能
```

### peacemindで想定される構成
```
Jira（issue管理）
    ↓
GitHub（コード管理・PR・レビュー）
    ↓
AWS（デプロイ先）
```

### 入社後に確認すること
- issueはJiraで管理しているか？
- プロジェクトキーは何か？（例：PM、PEACEなど）
- コミットメッセージのルールはあるか？

---

## 構成

- **バックエンド**: Spring Boot 3.5.12 / Java 21
- **デプロイ先**: Heroku（`powerful-dawn-80344`）
- **フロントエンド学習**: Vue 3（`vuetube_learning.html`）

---

## Heroku デプロイ

```bash
# コードをHerokuにプッシュ（自動デプロイ）
git push heroku master

# アプリを開く
heroku open -a salty-cove-04254
```

**URL**: https://salty-cove-04254.herokuapp.com

### エンドポイント

| URL | レスポンス |
|-----|-----------|
| `/` | Hello from Heroku! |
| `/api/status` | Application is running successfully! |

---

## PostgreSQL + JPA メモ

### 現在の構成

| サービス | 内容 | 月額 |
|---------|------|------|
| Heroku Dyno (Basic) | Spring Boot実行環境 | $7 |
| Heroku Postgres (Essential-0) | PostgreSQL | $5 |
| **合計** | | **$12/月** |

**アプリURL**: https://powerful-dawn-80344-bb5bfdab213d.herokuapp.com

### エンドポイント

| メソッド | URL | 説明 |
|---------|-----|------|
| GET | `/` | Hello from Heroku! |
| GET | `/api/status` | ステータス確認 |
| GET | `/api/messages` | メッセージ一覧（SELECT * FROM messages） |
| POST | `/api/messages` | メッセージ登録（INSERT INTO messages） |

### JpaRepositoryとは

SQLを書かずにDBを操作できる仕組み。継承するだけで主要なメソッドが使える。

```java
// これだけでSQLが自動生成される
public interface MessageRepository extends JpaRepository<Message, Long> {}

messageRepository.findAll();       // SELECT * FROM messages
messageRepository.save(message);   // INSERT INTO messages
messageRepository.deleteById(id);  // DELETE FROM messages WHERE id = ?
messageRepository.count();         // SELECT COUNT(*) FROM messages
```

### Heroku Postgres → Aurora Serverless v2（AWS移行時）

**AuroraはPostgreSQL互換** なので、Javaのコードは変更不要。
変わるのは `application.properties` の接続URLのみ。

```properties
# Heroku
spring.datasource.url=${DATABASE_URL}

# AWS Aurora（移行後）
spring.datasource.url=jdbc:postgresql://aurora-endpoint:5432/peacemind
```

### 課金を止める方法

```bash
# アプリごと削除
heroku apps:destroy powerful-dawn-80344
```

---

## Vue 3 学習メモ（vuetube_learning.html）

YouTube風UIでVue 3を学ぶ学習ファイル。

### 主な概念

#### `ref()` - リアクティブなデータ
```js
const count = ref(0);
// JS内は .value でアクセス
count.value++;
// テンプレートでは .value 不要
// {{ count }}
```

#### `computed()` - 自動計算される値
元のデータから導き出される値を定義する。依存するデータが変わると自動で再計算される。
```js
const totalAmount = computed(() => {
  return items.value.reduce((sum, item) => sum + item.price, 0);
});
```
- キャッシュがあるので高速
- 帳票の合計・件数の自動計算などに使う
- **値を「返したい」時に使う**

#### `watch()` - データの変化を監視
データが変化した時に処理を実行する。
```js
watch(searchQuery, async (newQuery) => {
  const res = await fetch(`/api/search?q=${newQuery}`);
  results.value = await res.json();
});
```
- APIを叩く・ログ出力などの副作用処理に使う
- サジェスト表示（入力するたびに自動検索）などに使う
- **処理を「実行したい」時に使う**

#### `function` - メソッド
ボタンクリックなど手動で実行する処理。
```js
function doSearch() {
  selectedVideo.value = null;
}
```
- ボタンを押してデータ取得する時に使う
- **手動で実行させたい時に使う**

### 使い分けまとめ

| シナリオ | 使うもの |
|---------|---------|
| 帳票の合計・件数の自動計算 | `computed` |
| ボタンを押してデータ取得 | `function` |
| 入力するたびにサジェスト表示 | `watch` |

### Vue 2 vs Vue 3

| | Vue 2（Options API） | Vue 3（Composition API） |
|---|---------------------|--------------------------|
| データ | `data()` セクション | `ref()` を setup() 内に書く |
| 計算値 | `computed:` セクション | `computed()` を setup() 内に書く |
| 関数 | `methods:` セクション | `function` を setup() 内に書く |
| サポート | 2023年12月終了 | 現在アクティブ開発中 |

Vue 3では機能ごとにコードがまとまるので、大規模開発でも読みやすい。

### 主なディレクティブ

| ディレクティブ | 説明 |
|--------------|------|
| `v-model` | 入力と双方向バインド |
| `v-for` | リストの繰り返し描画 |
| `v-if` / `v-else` | 条件で表示切り替え |
| `v-show` | display:noneで表示切り替え（DOMは残る） |
| `@click` | クリックイベント（v-on:clickの省略） |
| `:class` | 動的にクラスを切り替え |

### 学習ロードマップ

```
✅ ref / computed / v-if / v-for  ← 今ここ
⬜ watch / ライフサイクル（onMounted等）
⬜ コンポーネント（props / emit）
⬜ Vue Router（ページ遷移）
⬜ Pinia（状態管理）
⬜ Nuxt.js
```
