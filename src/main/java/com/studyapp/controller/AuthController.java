package com.studyapp.controller;

import com.studyapp.entity.User;
import com.studyapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired(required = false)  // 의존성 주입 문제 방지
    private UserRepository userRepository;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth Controller is working!");
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 의존성 체크
            if (userRepository == null) {
                response.put("success", false);
                response.put("message", "UserRepository not found");
                return ResponseEntity.internalServerError().body(response);
            }

            if (passwordEncoder == null) {
                response.put("success", false);
                response.put("message", "PasswordEncoder not found");
                return ResponseEntity.internalServerError().body(response);
            }

            String email = request.get("email");
            String password = request.get("password");
            String name = request.get("name");

            System.out.println("Signup request - Email: " + email + ", Name: " + name);

            if (email == null || password == null || name == null) {
                response.put("success", false);
                response.put("message", "이메일, 비밀번호, 이름을 모두 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (userRepository.existsByEmail(email)) {
                response.put("success", false);
                response.put("message", "이미 사용 중인 이메일입니다.");
                return ResponseEntity.badRequest().body(response);
            }

            User user = new User(email, passwordEncoder.encode(password), name);
            userRepository.save(user);

            response.put("success", true);
            response.put("message", "회원가입이 완료되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Signup error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "서버 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = request.get("email");
            String password = request.get("password");

            System.out.println("Login request - Email: " + email);

            if (email == null || password == null) {
                response.put("success", false);
                response.put("message", "이메일과 비밀번호를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (userRepository == null || passwordEncoder == null) {
                response.put("success", false);
                response.put("message", "서버 설정 오류");
                return ResponseEntity.internalServerError().body(response);
            }

            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
                User user = userOpt.get();
                response.put("success", true);
                response.put("user", Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "name", user.getName(),
                        "createdAt", user.getCreatedAt().toString()
                ));
                response.put("token", "dummy-jwt-token-" + user.getId());
                response.put("message", "로그인 성공");
                return ResponseEntity.ok(response);
            }

            response.put("success", false);
            response.put("message", "이메일 또는 비밀번호가 올바르지 않습니다.");
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "서버 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}