package com.example.demo;

import jakarta.persistence.*;

// ★ ポリモーフィズムのポイント3:
// @DiscriminatorValue → dtypeに保存される値
// Memberを継承して、社員固有の振る舞いを実装
@Entity
@DiscriminatorValue("EMPLOYEE")
public class Employee extends Member {

    private String position; // 役職

    public Employee() {}

    public Employee(String name, String department, String position) {
        super(name, department);
        this.position = position;
    }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    // ★ ポリモーフィズムのポイント4:
    // 同じ getRoleLabel() でも、Employeeでは「社員」を返す
    @Override
    public String getRoleLabel() {
        return "社員";
    }

    @Override
    public String[] getPermissions() {
        return new String[]{"閲覧", "編集"};
    }
}
