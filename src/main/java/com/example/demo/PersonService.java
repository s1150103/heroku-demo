package com.example.demo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    // ★ トランザクションのポイント1: @Transactional(readOnly = true)
    // → 読み取り専用トランザクション
    // → DBへの書き込みをブロックし、パフォーマンスが向上する
    @Transactional(readOnly = true)
    public List<Person> findAll() {
        return personRepository.findAll();
    }

    // ★ トランザクションのポイント2: @Transactional
    // → このメソッド内のDB操作を1つのトランザクションとして扱う
    // → 例外が発生した場合、自動でロールバックされる
    @Transactional
    public Person save(String name) {
        // バリデーション：名前が空の場合はエラー
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("名前を入力してください");
        }
        return personRepository.save(new Person(name.trim()));
    }

    // ★ トランザクションのポイント3: 複数のDB操作を1つにまとめる
    // → 1が成功して2が失敗した場合、1もロールバックされる
    @Transactional
    public void saveMultiple(List<String> names) {
        for (String name : names) {
            // バリデーション：1つでも空があれば全件ロールバック
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("空の名前が含まれています。全件登録を取り消します");
            }
            personRepository.save(new Person(name.trim()));
        }
    }

    // ★ トランザクションのポイント4: @Transactional
    // → 削除もトランザクション管理（失敗時はロールバック）
    @Transactional
    public void delete(Long id) {
        personRepository.deleteById(id);
    }
}
