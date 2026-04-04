package com.example.demo;

import org.springframework.stereotype.Service;
import java.util.List;

// ★ DIのポイント1: @Service をつけるとSpringがこのクラスを管理する
// → 他のクラスから自動で注入（DI）できるようになる
@Service
public class PersonService {

    // ★ DIのポイント2: PersonRepositoryをSpringが自動で注入してくれる
    // → new PersonRepository() と書かなくていい
    private final PersonRepository personRepository;

    // ★ DIのポイント3: コンストラクタインジェクション
    // → Springがこのコンストラクタを呼び出してPersonRepositoryを渡してくれる
    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    // 全件取得（SELECT * FROM persons）
    public List<Person> findAll() {
        return personRepository.findAll();
    }

    // 名前を登録（INSERT INTO persons）
    public Person save(String name) {
        // バリデーション：名前が空の場合はエラー
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("名前を入力してください");
        }
        return personRepository.save(new Person(name.trim()));
    }

    // 削除（DELETE FROM persons WHERE id = ?）
    public void delete(Long id) {
        personRepository.deleteById(id);
    }
}
