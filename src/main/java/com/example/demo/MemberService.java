package com.example.demo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

// ★ DIのポイント: @ServiceでSpringが管理、MemberRepositoryを自動注入
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 全メンバー取得
    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    // 種別で絞り込み
    @Transactional(readOnly = true)
    public List<Member> findByType(String type) {
        return memberRepository.findByDtype(type);
    }

    // ★ ポリモーフィズムのポイント7:
    // typeによって生成するオブジェクトを切り替える（Factory的な使い方）
    // → 呼び出し側は型を意識しなくていい
    @Transactional
    public Member save(MemberController.MemberRequest req) {
        return switch (req.getType()) {
            case "EMPLOYEE" -> memberRepository.save(
                new Employee(req.getName(), req.getDepartment(), req.getExtra())
            );
            case "PART_TIMER" -> memberRepository.save(
                new PartTimer(req.getName(), req.getDepartment(),
                    req.getExtra() != null ? Integer.parseInt(req.getExtra()) : 0)
            );
            case "ADMIN" -> memberRepository.save(
                new Admin(req.getName(), req.getDepartment(), req.getExtra())
            );
            default -> throw new IllegalArgumentException("不明な種別: " + req.getType());
        };
    }

    // 削除
    @Transactional
    public void delete(Long id) {
        memberRepository.deleteById(id);
    }

    // ★ ポリモーフィズムのポイント8:
    // 同じリスト（List<Member>）でも、各要素のgetRoleLabel()は
    // それぞれのサブクラスの実装が呼ばれる
    @Transactional(readOnly = true)
    public List<MemberDTO> findAllAsDTO() {
        return memberRepository.findAll().stream()
            .map(m -> new MemberDTO(
                m.getId(),
                m.getName(),
                m.getDepartment(),
                m.getRoleLabel(),      // ← Employeeなら「社員」、Adminなら「管理者」
                m.getPermissions()     // ← それぞれ異なる権限リスト
            ))
            .toList();
    }
}
