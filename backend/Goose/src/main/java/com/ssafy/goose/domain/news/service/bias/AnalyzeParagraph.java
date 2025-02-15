package com.ssafy.goose.domain.news.service.bias;

import com.ssafy.goose.domain.news.entity.ReferenceNewsArticle;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AnalyzeParagraph {

    // ✅ FastAPI 서버 URL (EC2 배포 시 주소 변경 필요)
    private static final String FASTAPI_URL = "http://i12d208.p.ssafy.io:5061/news/reliability";
//    private static final String FASTAPI_URL = "http://localhost:5061/news/reliability";
    private final RestTemplate restTemplate;

    public AnalyzeParagraph() {
        this.restTemplate = new RestTemplate();
    }

    public ParagraphAnalysisResult analyze(String title, List<String> paragraphs) {
        // ✅ FastAPI 요청 데이터 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("news", Map.of(
                "title", title,
                "paragraphs", paragraphs
        ));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(FASTAPI_URL, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Double> reliabilityScores = (List<Double>) response.getBody().get("paragraph_reliability_scores");
                List<String> bestMatches = (List<String>) response.getBody().get("best_evidence_paragraphs");

                // ✅ best_matches를 콘솔에 출력
//                System.out.println("✅ FastAPI 신뢰성 분석 결과:");
//                for (int i = 0; i < bestMatches.size(); i++) {
//                    System.out.println("🔹 문단 " + (i + 1) + " 신뢰성 점수: " + reliabilityScores.get(i));
//                    System.out.println();
//                    System.out.println("🔹 기존 문단 : ");
//                    System.out.println(paragraphs.get(i));
//                    System.out.println();
//                    System.out.printf("🔹%.2f%% 확률로 올바른 내용\n", reliabilityScores.get(i) * 100);
//                    System.out.println("   ➜ " + bestMatches.get(i));
//                    System.out.println();
//                    System.out.println();
//                }

                return new ParagraphAnalysisResult(reliabilityScores, bestMatches);
            }
        } catch (Exception e) {
            System.err.println("❌ FastAPI 요청 실패: " + e.getMessage());
        }

        // 오류 발생 시 기본값 반환
        return new ParagraphAnalysisResult(List.of(50.0), List.of());
    }
}
