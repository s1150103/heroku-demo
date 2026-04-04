package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

// ★ Spring Securityの設定クラス
// @Configuration → Springがこのクラスを設定として読み込む
// @EnableWebSecurity → Spring Securityを有効にする
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ★ パスワードの暗号化方式を定義
    // BCrypt = 業界標準の強力なハッシュアルゴリズム
    // 平文パスワードをそのままDBに保存するのはNG（セキュリティ上の基本）
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ★ ユーザー情報をメモリ上に定義（本番ではDBから取得する）
    // ピースマインドの本番環境では、相談員・管理者・一般社員などのロールが必要
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // ADMINロール: 全操作可能（会員の登録・削除・閲覧）
        var admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        // USERロール: 閲覧のみ可能（会員の削除は不可）
        var user = User.builder()
                .username("user")
                .password(passwordEncoder.encode("user123"))
                .roles("USER")
                .build();

        // ★ InMemoryUserDetailsManager = メモリ上でユーザー管理
        // 学習用途。本番ではUserDetailsServiceをDBで実装する
        return new InMemoryUserDetailsManager(admin, user);
    }

    // ★ URLごとのアクセス制御を定義するメインの設定
    // どのURLに誰がアクセスできるかをここで決める
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ★ CSRF保護: REST APIではCSRFトークン不要なので無効化
            // （ブラウザのフォーム送信ではなく、APIクライアントからのリクエストのため）
            .csrf(csrf -> csrf.disable())

            // ★ 認可ルール: URLとロールの対応を定義
            .authorizeHttpRequests(auth -> auth

                // GETリクエスト（閲覧）→ USERロール以上なら許可
                .requestMatchers(HttpMethod.GET, "/api/members/**").hasAnyRole("USER", "ADMIN")

                // DELETE（削除）→ ADMINロールのみ許可
                // ピースマインドでは管理者だけが会員を削除できるイメージ
                .requestMatchers(HttpMethod.DELETE, "/api/members/**").hasRole("ADMIN")

                // POST（登録）→ ADMINロールのみ許可
                .requestMatchers(HttpMethod.POST, "/api/members/**").hasRole("ADMIN")

                // その他のリクエストは全て認証が必要
                .anyRequest().authenticated()
            )

            // ★ Basic認証を有効化（ブラウザやcurlでID/パスワードを送る方式）
            // 本番ではJWTトークン認証に切り替えるのが一般的
            .httpBasic(basic -> {});

        return http.build();
    }
}
