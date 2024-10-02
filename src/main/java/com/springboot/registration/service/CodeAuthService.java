package com.springboot.registration.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
public class CodeAuthService {
    @Value("${codef.client-id}")
    private String clientId;

    @Value("${codef.client-secret}")
    private String clientSecret;

    // RestTemplate 인스턴스 생성 (HTTP 요청을 보내기 위해 사용)
    private final RestTemplate restTemplate = new RestTemplate();

    // 액세스 토큰을 얻기 위한 메서드
    public String getAccessToken() {
        String tokenUrl = "https://oauth.codef.io/oauth/token";

        // 요청 본문의 형식을 지정 : 폼 데이터 형식
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Authorization 헤더는 서버에 클라이언트의 인증 정보를 전달
        // Basic 인증 방식
        // Basic 인증은 'username:password' 형식의 문자열을 Base64로 인코딩하여 사용
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);

        // 서버로 전송할 데이터
        // grant_type을 client_credentials로 설정하여 클라이언트 자격 증명 방식의 OAuth 인증을 요청.
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        // HttpEntity는 HTTP 요청의 전체 내용을 나타내는 객체
        // 헤더와 바디를 포함하여 하나의 요청 객체로 만듬
        // RestTemplate을 통해 서버로 전송
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        System.out.println("Requesting Token from URL: " + tokenUrl);
        System.out.println("Request Body: " + body);
        System.out.println("Request Headers: " + headers);

        try {
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(tokenUrl, HttpMethod.POST, request, new ParameterizedTypeReference<>() {});

            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("access_token")) {
                return (String) responseBody.get("access_token");
            } else {
                throw new RuntimeException("Failed to get access token: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            System.out.println("Error occurred: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            System.out.println("Request URL: " + tokenUrl);
            System.out.println("Request Body: " + body);
            System.out.println("Request Headers: " + headers);
            throw e;
        }
    }
}
