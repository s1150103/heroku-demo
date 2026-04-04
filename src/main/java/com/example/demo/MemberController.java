package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = "*")
public class MemberController {

    // ★ DI: MemberServiceをSpringが自動注入
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 全メンバー一覧（ポリモーフィズムでroleLabel・permissionsが自動設定）
    @GetMapping
    public List<MemberDTO> getAll() {
        return memberService.findAllAsDTO();
    }

    // メンバー登録
    @PostMapping
    public Member create(@RequestBody MemberRequest req) {
        return memberService.save(req);
    }

    // 削除
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        memberService.delete(id);
    }

    // リクエストボディ用クラス
    static class MemberRequest {
        private String name;
        private String department;
        private String type;    // EMPLOYEE / PART_TIMER / ADMIN
        private String extra;   // 役職 / 週時間 / 管理者レベル

        public String getName() { return name; }
        public String getDepartment() { return department; }
        public String getType() { return type; }
        public String getExtra() { return extra; }
        public void setName(String name) { this.name = name; }
        public void setDepartment(String department) { this.department = department; }
        public void setType(String type) { this.type = type; }
        public void setExtra(String extra) { this.extra = extra; }
    }
}
