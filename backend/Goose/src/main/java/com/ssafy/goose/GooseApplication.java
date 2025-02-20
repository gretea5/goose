package com.ssafy.goose;

import com.ssafy.goose.domain.news.scheduler.AutoCrawlingService;
import com.ssafy.goose.domain.news.service.bias.BiasAnalyseService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 🔹 @Scheduled 활성화
public class GooseApplication {

	public static void main(String[] args) {
		SpringApplication.run(GooseApplication.class, args);
	}

	// 🔹 앱 시작 후 즉시 뉴스 크롤링 실행 및 제목 검증 테스트
	@Bean
	public CommandLineRunner run(
			AutoCrawlingService autoCrawlingService,
			BiasAnalyseService biasAnalyseService
	) {
		return args -> {
			// ✅ 자동 뉴스 크롤링
			 System.out.println("🚀 애플리케이션 실행 후 즉시 뉴스 크롤링 시작...");
			 autoCrawlingService.fetchAndSaveTrendingNews();
		};
	}
}
