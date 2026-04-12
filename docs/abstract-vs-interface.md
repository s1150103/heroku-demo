# abstractクラス vs interface

## 結論：このプロジェクトがabstractを選んだ理由

Member クラスは「DBで管理するデータ」と「共通の構造」を両方持つ必要があった。
それができるのは abstract クラスだけ。

---

## 違いの一覧

| 比較項目 | interface | abstract クラス |
|---|---|---|
| フィールドを持てる | できない | **できる** |
| コンストラクタを持てる | できない | **できる** |
| 共通の処理を書ける | できない | **できる** |
| `new` でインスタンス化 | できない | できない |
| 複数継承 | **できる** | できない（1つのみ） |
| JPAの `@Entity` を付けられる | できない | **できる** |

---

## interfaceとは

「できること」を定義するもの。
状態（フィールド）は持てない。ルールだけを定義する。

```java
interface Printable {
    void print(); // 実装は持てない（ルールだけ）
}

interface Exportable {
    void export();
}

// 複数のinterfaceを同時に実装できる
class Report implements Printable, Exportable {
    void print()  { ... }
    void export() { ... }
}
```

**向いているケース：**
- 「印刷できる」「エクスポートできる」など、能力を定義したいとき
- 複数の異なるクラスに同じ能力を持たせたいとき

---

## abstract クラスとは

「共通の状態と構造」を定義するもの。
フィールドや共通処理を子クラスに引き継がせる。

```java
public abstract class Member {
    // フィールド（状態）を持てる ← interfaceにはできない
    private String name;
    private String department;

    // 抽象メソッド → 子クラスで必ず実装しなければならない
    public abstract String getRoleLabel();
    public abstract String[] getPermissions();
}
```

**向いているケース：**
- 共通のフィールドを子クラスに引き継がせたいとき
- DBとマッピングしたいとき（`@Entity`が必要）
- 「直接インスタンス化させたくない」親クラスを作るとき

---

## このプロジェクトで abstract を選んだ理由

### 理由1：共通フィールドを持つ必要があった

Employee・PartTimer・Admin は全員 `name` と `department` を持つ。
これを1箇所にまとめるには abstract クラスが必要。

```java
// Member に書くことで、全サブクラスが自動的に持つ
private String name;
private String department;
```

interface には フィールドを書けないので不可能。

---

### 理由2：JPAでDBに保存する必要があった

```java
@Entity                                        // DBテーブルと紐付け
@Inheritance(strategy = SINGLE_TABLE)          // 1テーブルで継承を管理
@DiscriminatorColumn(name = "dtype")           // 種別を区別するカラム
public abstract class Member {
```

`@Entity` は class にしか付けられない。
interface に付けることはできないので、abstract クラスにする必要があった。

---

### 理由3：直接 new させたくなかった

```java
// これをやらせたくない
Member m = new Member(); // コンパイルエラー

// こうさせたい
Member m = new Employee(...); // OK
Member m = new PartTimer(...); // OK
Member m = new Admin(...);    // OK
```

abstract にすることで `new Member()` を禁止できる。
必ずサブクラスを通じてインスタンス化させる設計。

---

## DBの中身（SINGLE_TABLEの結果）

| id | dtype | name | department | position | hoursPerWeek | adminLevel |
|---|---|---|---|---|---|---|
| 1 | EMPLOYEE | 田中 | 開発部 | エンジニア | null | null |
| 2 | PART_TIMER | 鈴木 | 総務部 | null | 20 | null |
| 3 | ADMIN | 山田 | 経営企画 | null | null | L1 |

全員が1つのテーブルに入り、`dtype` で種別を区別している。

---

## まとめ

```
interface  → 「できること」のルールを定義する
abstract   → 「共通の状態と構造」を定義する

このプロジェクトは：
  - name・department という状態を共有したい
  - @Entity でDBに保存したい
  - new Member() を禁止したい

→ 全部できるのは abstract クラスだけ
```
