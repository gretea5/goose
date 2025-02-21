package com.ssafy.goose.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api") // 기본 URL 경로 설정
public class ApiTestController {

    @GetMapping("/hello") // GET 요청 처리
    public String hello() {
        return "Hello, D208 Spring Boot Test!";
    }
}
