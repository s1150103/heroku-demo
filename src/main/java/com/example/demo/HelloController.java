package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class HelloController {

    private final MessageRepository messageRepository;

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

    // メッセージ一覧取得
    @GetMapping("/api/messages")
    public List<Message> getMessages() {
        return messageRepository.findAll();
    }

    // メッセージ登録
    @PostMapping("/api/messages")
    public Message createMessage(@RequestBody Message message) {
        return messageRepository.save(message);
    }
}
