package com.example.demo;

// フロントエンドに返すデータ構造
// getRoleLabel()・getPermissions()の結果をまとめて返す
public class MemberDTO {
    private Long id;
    private String name;
    private String department;
    private String roleLabel;
    private String[] permissions;

    public MemberDTO(Long id, String name, String department, String roleLabel, String[] permissions) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.roleLabel = roleLabel;
        this.permissions = permissions;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public String getRoleLabel() { return roleLabel; }
    public String[] getPermissions() { return permissions; }
}
