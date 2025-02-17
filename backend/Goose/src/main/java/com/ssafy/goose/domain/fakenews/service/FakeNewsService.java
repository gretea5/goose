package com.ssafy.goose.domain.fakenews.service;

import com.ssafy.goose.domain.fakenews.entity.FakeNews;
import com.ssafy.goose.domain.fakenews.repository.mongo.FakeNewsRepository;
import com.ssafy.goose.domain.fakenews.repository.jpa.FakeNewsGameResultRepository;
import com.ssafy.goose.domain.fakenews.entity.FakeNewsGameResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FakeNewsService {

    private final FakeNewsRepository fakeNewsRepository;
    private final FakeNewsGameResultRepository gameResultRepository;

    public FakeNewsService(FakeNewsRepository fakeNewsRepository,
                           FakeNewsGameResultRepository gameResultRepository) {
        this.fakeNewsRepository = fakeNewsRepository;
        this.gameResultRepository = gameResultRepository;
    }

    // 로그인 사용자의 경우 이미 푼 뉴스는 제외하여 랜덤으로 반환
    // 비로그인 사용자는 단순 랜덤 반환
    public FakeNews getRandomFakeNews(String username) {
        List<FakeNews> allNews = fakeNewsRepository.findAll();
        if (!"guest".equals(username)) {
            // 해당 사용자가 이미 푼 뉴스 ID 목록 조회 (MySQL에 저장된 결과 이용)
            List<String> solvedIds = gameResultRepository.findByUsername(username)
                    .stream().map(FakeNewsGameResult::getNewsId)
                    .collect(Collectors.toList());
            allNews = allNews.stream()
                    .filter(news -> !solvedIds.contains(news.getId()))
                    .collect(Collectors.toList());
        }
        if (allNews.isEmpty()) {
            return null;
        }
        Random random = new Random();
        int index = random.nextInt(allNews.size());
        return allNews.get(index);
    }

    public void updateNewsStatistics(String newsId, String userChoice, long dwellTime, String nickname) {
        Optional<FakeNews> optionalNews = fakeNewsRepository.findById(newsId);
        if (!optionalNews.isPresent()) {
            return;
        }
        FakeNews news = optionalNews.get();

        // 투표수 업데이트
        Map<String, Integer> votes = news.getVoteCounts();
        votes.put(userChoice, votes.getOrDefault(userChoice, 0) + 1);

        // 전체 투표수와 선택 비율 업데이트
        int totalVotes = votes.values().stream().mapToInt(Integer::intValue).sum();
        Map<String, Double> percentages = new HashMap<>();
        for (Map.Entry<String, Integer> entry : votes.entrySet()) {
            percentages.put(entry.getKey(), (entry.getValue() * 100.0) / totalVotes);
        }
        news.setSelectionPercentages(percentages);

        // 체류시간 랭킹 업데이트 (낮은 시간이 우수)
        List<FakeNews.Ranking> rankings = news.getDwellTimeRanking();
        if (rankings == null) {
            rankings = new ArrayList<>();
        }
        if (rankings.size() < 3) {
            rankings.add(new FakeNews.Ranking(nickname, dwellTime));
        } else {
            // Top3 중 가장 높은 체류시간(최악)과 비교
            Optional<FakeNews.Ranking> worstOpt = rankings.stream()
                    .max(Comparator.comparingLong(FakeNews.Ranking::getDwellTime));
            if (worstOpt.isPresent() && dwellTime < worstOpt.get().getDwellTime()) {
                rankings.remove(worstOpt.get());
                rankings.add(new FakeNews.Ranking(nickname, dwellTime));
            }
        }
        // 낮은 순으로 정렬 후 저장
        rankings.sort(Comparator.comparingLong(FakeNews.Ranking::getDwellTime));
        news.setDwellTimeRanking(rankings);

        fakeNewsRepository.save(news);
        log.info("Updated statistics for newsId {} with choice {} and dwellTime {}", newsId, userChoice, dwellTime);
    }
}
