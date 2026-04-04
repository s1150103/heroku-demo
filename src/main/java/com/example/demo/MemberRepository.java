package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// ★ JpaRepositoryでMember（親クラス）を扱う
// → Employee・PartTimer・Adminも全て取得できる（ポリモーフィズム）
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 種別で絞り込み（dtypeカラムで検索）
    List<Member> findByDtype(String dtype);

    // 部署で絞り込み
    List<Member> findByDepartment(String department);
}
