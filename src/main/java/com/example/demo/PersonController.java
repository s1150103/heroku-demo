package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/persons")
// CORS設定：Vue.jsからのアクセスを許可
@CrossOrigin(origins = "*")
public class PersonController {

    // ★ DIのポイント4: PersonServiceをSpringが自動で注入してくれる
    // → PersonControllerはPersonServiceの作り方を知らなくていい
    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    // 一覧取得
    @GetMapping
    public List<Person> getAll() {
        return personService.findAll();
    }

    // 登録
    @PostMapping
    public Person create(@RequestBody PersonRequest request) {
        return personService.save(request.getName());
    }

    // 複数登録（トランザクション：1つでも失敗したら全件ロールバック）
    @PostMapping("/bulk")
    public List<Person> createMultiple(@RequestBody BulkRequest request) {
        personService.saveMultiple(request.getNames());
        return personService.findAll();
    }

    // 削除
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        personService.delete(id);
    }

    // リクエストボディ用クラス
    static class PersonRequest {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    static class BulkRequest {
        private List<String> names;
        public List<String> getNames() { return names; }
        public void setNames(List<String> names) { this.names = names; }
    }
}
