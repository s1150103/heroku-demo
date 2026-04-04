# peacemind 練習用プロジェクト

Spring Boot + Heroku + Vue.js の学習用リポジトリ。

---

## 構成

- **バックエンド**: Spring Boot 3.5.12 / Java 21
- **デプロイ先**: Heroku（`salty-cove-04254`）
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
