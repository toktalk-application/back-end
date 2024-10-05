package com.springboot.auth.service;

import com.springboot.auth.jwt.JwtTokenizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {
    private final JwtTokenizer jwtTokenizer;

    /**
     * AuthService 생성자
     *
     * JwtTokenizer를 주입받아 초기화합니다.
     * JwtTokenizer는 JWT 관련 토큰 생성, 검증, 삭제 등의 기능을 제공하는 유틸리티 클래스입니다.
     *
     * @param jwtTokenizer JWT 관련 작업을 수행하는 유틸리티 클래스
     */
    public AuthService(JwtTokenizer jwtTokenizer) {
        this.jwtTokenizer = jwtTokenizer;
    }

    /**
     * 사용자를 로그아웃 처리하는 메서드
     *
     * 이 메서드는 주어진 사용자명에 해당하는 JWT 토큰을 삭제하여 로그아웃을 처리합니다.
     * 주로 Redis와 같은 저장소에서 토큰을 삭제하는 방식으로 로그아웃이 구현됩니다.
     *
     * @param username 로그아웃할 사용자의 사용자명
     * @return 로그아웃이 성공하면 true, 실패하면 false를 반환
     */
    public boolean logout(String username) {
        return jwtTokenizer.deleteRegisterToken(username); // JwtTokenizer를 사용하여 저장된 토큰을 삭제합니다.
    }
}