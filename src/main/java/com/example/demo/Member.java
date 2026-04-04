package com.example.demo;

import jakarta.persistence.*;

// ★ ポリモーフィズムのポイント1:
// @Inheritance(strategy = SINGLE_TABLE) → 1つのテーブルで継承を表現
// dtype列で種別を管理する
@Entity
@Table(name = "members")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
public abstract class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String department;

    public Member() {}

    public Member(String name, String department) {
        this.name = name;
        this.department = department;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public void setName(String name) { this.name = name; }
    public void setDepartment(String department) { this.department = department; }

    // ★ ポリモーフィズムのポイント2:
    // 抽象メソッド → サブクラスで必ず実装する
    // 同じメソッド名でも、サブクラスによって異なる振る舞いをする
    public abstract String getRoleLabel();
    public abstract String[] getPermissions();
}
