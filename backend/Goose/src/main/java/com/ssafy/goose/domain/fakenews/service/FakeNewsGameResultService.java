package com.ssafy.goose.domain.fakenews.service;

import com.ssafy.goose.domain.fakenews.entity.FakeNewsGameResult;
import com.ssafy.goose.domain.fakenews.repository.jpa.FakeNewsGameResultRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class FakeNewsGameResultService {

    private final FakeNewsGameResultRepository gameResultRepository;

    public FakeNewsGameResultService(FakeNewsGameResultRepository gameResultRepository) {
        this.gameResultRepository = gameResultRepository;
    }

    // 게임 결과를 저장하고 반환
    public FakeNewsGameResult saveGameResult(String username, String newsId, String userChoice,
                                             Boolean correct, long dwellTime, int totalQuestions, int correctCount) {
        FakeNewsGameResult result = new FakeNewsGameResult();
        result.setUsername(username);
        result.setNewsId(newsId);
        result.setUserChoice(userChoice);
        result.setCorrect(correct);
        result.setDwellTime(dwellTime);
        result.setTotalQuestions(totalQuestions);
        result.setCorrectCount(correctCount);
        result.setSolvedAt(LocalDateTime.now());
        gameResultRepository.save(result);

        return result;
    }
}
