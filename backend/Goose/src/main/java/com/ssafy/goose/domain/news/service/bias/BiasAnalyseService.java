package com.ssafy.goose.domain.news.service.bias;

import com.ssafy.goose.domain.contentsearch.dto.KeywordResponseDto;
import com.ssafy.goose.domain.contentsearch.service.KeywordService;
import com.ssafy.goose.domain.news.entity.ReferenceNewsArticle;
import com.ssafy.goose.domain.news.repository.ReferenceNewsCustomRepository;
import com.ssafy.goose.domain.news.service.keyword.TitleKeywordExtractor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BiasAnalyseService {
    private final ReferenceNewsCustomRepository referenceNewsCustomRepository;
    private final AnalyseByTitle analyseByTitle;
    private final AnalyseByContent analyseByContent;
    private final TitleKeywordExtractor keywordExtractorService;
    private final KeywordService keywordService;
    private final AnalyzeParagraph analyzeParagraph;

    public BiasAnalyseService(
            ReferenceNewsCustomRepository referenceNewsCustomRepository,
            AnalyseByTitle analyseByTitle,
            AnalyseByContent analyseByContent,
            KeywordService keywordService,
            TitleKeywordExtractor keywordExtractorService,
            AnalyzeParagraph analyzeParagraph) {
        this.referenceNewsCustomRepository = referenceNewsCustomRepository;
        this.analyseByTitle = analyseByTitle;
        this.analyseByContent = analyseByContent;
        this.keywordService = keywordService;
        this.keywordExtractorService = keywordExtractorService;
        this.analyzeParagraph = analyzeParagraph;
    }

    public BiasAnalysisResult analyzeBiasMyNews(String title, String content, List<String> paragraphs) {
        System.out.println("analyzeBias 수행, title : " + title);
        // 1. 내용에서 주요 키워드 3개 추출
//      List<String> keywords = keywordExtractorService.extractTopKeywords(title, 3);
//        KeywordResponseDto keywordResponse = keywordService.extractKeywords(content);
//        String[] keywords = keywordResponse.getKeywords();
//
//        if (keywords == null || keywords.length < 3) {
//            System.out.println("❌ 추출된 키워드가 부족합니다. 기본 점수를 반환합니다.");
//            return BiasAnalysisResult.builder()
//                    .biasScore(50.0)
//                    .reliability(50.0)
//                    .paragraphReliabilities(null)
//                    .paragraphReasons(null)
//                    .build();
//        }
//        System.out.println("🔹 추출된 키워드: " + keywords[0] + ", " + keywords[1] + ", " + keywords[2]);


        // 2. 3일 이내 키워드 기반 레퍼런스 뉴스 검색
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<ReferenceNewsArticle> referenceNews = referenceNewsCustomRepository.findNewsByKeywords(title, content);

        if (referenceNews.isEmpty()) {
            System.out.println("❌ 해당 키워드와 관련된 최근 뉴스 없음");
            return BiasAnalysisResult.builder()
                    .biasScore(50.0)
                    .reliability(50.0)
                    .paragraphReliabilities(null)
                    .paragraphReasons(null)
                    .build();
        }
//
//        // 3. 검색된 레퍼런스 뉴스 기사들의 본문 내용 추출
//        List<String> referenceContents = referenceNews.stream()
//                .map(ReferenceNewsArticle::getContent)
//                .collect(Collectors.toList());
//
//        if (referenceContents.size() < 2) {
//            System.out.println("❌ 비교할 레퍼런스 뉴스 문단이 부족함");
//            return BiasAnalysisResult.builder()
//                    .biasScore(50.0)
//                    .reliability(50.0)
//                    .paragraphReliabilities(null)
//                    .paragraphReasons(null)
//                    .build();
//        }
//
        // 4. 제목으로 분석 : FastAPI 서버로 NLP 검증 요청
        double bias_title = analyseByTitle.checkTitleWithReference(title, referenceNews);
//
//        // 5. 내용으로 분석 : FastAPI 서버로 NLP 검증 요청
//        double bias_content = analyseByContent.checkContentWithReference(content, referenceContents);

        // 6. 문단 신뢰성 분석 요청 (FastAPI 호출)
        ParagraphAnalysisResult analysisResult = analyzeParagraph.analyze(title, paragraphs);
        double paragraph_reliability = analysisResult.getAverageReliability();

//        double finalScore = (bias_title + bias_content + paragraph_reliability) / 3;
        double finalScore = paragraph_reliability;

        return BiasAnalysisResult.builder()
                .biasScore(finalScore)
                .reliability(finalScore)
                .paragraphReliabilities(analysisResult.getReliabilityScores())
                .paragraphReasons(analysisResult.getBestMatches())
                .build();
    }

    public BiasAnalysisResult analyzeBiasExternalNews(String id, String title, String content, List<String> paragraphs) {
        System.out.println("analyzeBias 수행, title : " + title);

        // 2. 레퍼런스 뉴스 검색
        List<ReferenceNewsArticle> referenceNewsList = referenceNewsCustomRepository.findNewsByKeywords(title, content);

        if (referenceNewsList.isEmpty()) {
            System.out.println("❌ 해당 키워드와 관련된 최근 뉴스 없음");
            return BiasAnalysisResult.builder()
                    .biasScore(50.0)
                    .reliability(50.0)
                    .paragraphReliabilities(null)
                    .paragraphReasons(null)
                    .build();
        }
//
//        // 3. 검색된 레퍼런스 뉴스 기사들의 본문 내용 추출
//        List<String> referenceContents = referenceNews.stream()
//                .map(ReferenceNewsArticle::getContent)
//                .collect(Collectors.toList());
//
//        if (referenceContents.size() < 2) {
//            System.out.println("❌ 비교할 레퍼런스 뉴스 문단이 부족함");
//            return BiasAnalysisResult.builder()
//                    .biasScore(50.0)
//                    .reliability(50.0)
//                    .paragraphReliabilities(null)
//                    .paragraphReasons(null)
//                    .build();
//        }
//
        // 4. 제목으로 분석 : FastAPI 서버로 NLP 검증 요청
        double bias_title = analyseByTitle.checkTitleWithReference(id, referenceNewsList);
//
//        // 5. 내용으로 분석 : FastAPI 서버로 NLP 검증 요청
//        double bias_content = analyseByContent.checkContentWithReference(content, referenceContents);

        // 6. 문단 신뢰성 분석 요청 (FastAPI 호출)
        ParagraphAnalysisResult analysisResult = analyzeParagraph.analyze(title, paragraphs);
        double paragraph_reliability = analysisResult.getAverageReliability();

//        double finalScore = (bias_title + bias_content + paragraph_reliability) / 3;
        double finalScore = paragraph_reliability;

        return BiasAnalysisResult.builder()
                .biasScore(finalScore)
                .reliability(finalScore)
                .paragraphReliabilities(analysisResult.getReliabilityScores())
                .paragraphReasons(analysisResult.getBestMatches())
                .build();
    }
}
