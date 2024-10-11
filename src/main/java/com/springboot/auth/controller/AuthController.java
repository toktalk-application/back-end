package com.springboot.auth.controller;

import com.springboot.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    /**
     * AuthController 생성자
     *
     * AuthService를 주입받아 초기화합니다.
     * AuthService는 인증 및 인가와 관련된 비즈니스 로직을 처리하는 서비스 계층입니다.
     *
     * @param authService 인증 및 인가를 처리하는 서비스 계층 객체
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 로그아웃 요청을 처리하는 메서드
     *
     * 클라이언트가 "/auth/logout" 경로로 POST 요청을 보내면 이 메서드가 호출됩니다.
     * 로그아웃 요청을 처리하고, 성공 여부에 따라 적절한 HTTP 상태 코드를 반환합니다.
     *
     * @param authentication 현재 인증된 사용자의 인증 정보를 담고 있는 객체
     * @return 로그아웃 성공 시 HTTP 200 OK, 실패 시 HTTP 403 Forbidden을 반환
     */
    @PostMapping("/logout") // "/auth/logout" 경로로 POST 요청을 처리하는 메서드로 지정합니다.
    public ResponseEntity postLogout(Authentication authentication) {
        String username = authentication.getName(); // 현재 인증된 사용자의 사용자명을 가져옵니다.

        // AuthService의 logout 메서드를 호출하여 로그아웃을 처리하고, 결과에 따라 HTTP 상태 코드를 반환합니다.
        return authService.logout(username) ?
                new ResponseEntity(HttpStatus.OK) : new ResponseEntity(HttpStatus.FORBIDDEN);
    }
}