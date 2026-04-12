package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

// ★ JpaRepositoryでMember（親クラス）を扱う
// → Employee・PartTimer・Adminも全て取得できる（ポリモーフィズム）
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 種別で絞り込み
    // ★ dtypeはJPAエンティティのフィールドではなくDBの内部カラムのため
    //    findByDtype()は使えない → @Queryで直接JPQLを書く
    @Query("SELECT m FROM Member m WHERE TYPE(m) = :type")
    List<Member> findByType(@Param("type") Class<?> type);

    // 部署で絞り込み（departmentはエンティティのフィールドなのでそのまま使える）
    List<Member> findByDepartment(String department);
}

