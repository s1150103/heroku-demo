package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class HelloController {

    // PostgreSQLと接続するためのRepository
    // MessageRepository経由でDBのmessagesテーブルを操作する
    private final MessageRepository messageRepository;

    // SpringがMessageRepositoryを自動で注入してくれる（DIコンテナ）
    public HelloController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @GetMapping("/")
    public String hello() {
        return "Hello from Heroku!";
    }

    @GetMapping("/api/status")
    public String status() {
        return "Application is running successfully!";
    }

    // [PostgreSQL連携] メッセージ一覧取得
    // SELECT * FROM messages に相当
    // findAll()はJpaRepositoryが提供するメソッド
    @GetMapping("/api/messages")
    public List<Message> getMessages() {
        return messageRepository.findAll();
    }

    // [PostgreSQL連携] メッセージ登録
    // INSERT INTO messages (content) VALUES (...) に相当
    // @RequestBody: リクエストのJSONをMessageオブジェクトに変換
    // save()はJpaRepositoryが提供するメソッド（INSERT/UPDATE）
    @PostMapping("/api/messages")
    public Message createMessage(@RequestBody Message message) {
        return messageRepository.save(message);
    }
}
