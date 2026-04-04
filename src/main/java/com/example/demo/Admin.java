package com.example.demo;

import jakarta.persistence.*;

// ★ ポリモーフィズムのポイント6:
// Adminは全権限を持つ → getPermissions()で全権限を返す
@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends Member {

    private String adminLevel; // 管理者レベル（例：L1, L2）

    public Admin() {}

    public Admin(String name, String department, String adminLevel) {
        super(name, department);
        this.adminLevel = adminLevel;
    }

    public String getAdminLevel() { return adminLevel; }
    public void setAdminLevel(String adminLevel) { this.adminLevel = adminLevel; }

    @Override
    public String getRoleLabel() {
        return "管理者";
    }

    @Override
    public String[] getPermissions() {
        return new String[]{"閲覧", "編集", "削除", "管理"};
    }
}
