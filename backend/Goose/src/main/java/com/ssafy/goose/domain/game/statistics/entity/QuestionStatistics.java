package com.ssafy.goose.domain.game.statistics.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "question_statistics")
public class QuestionStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false)
    @Builder.Default    // Builder 패턴 사용시 기본값 설정
    private Integer option1Count = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer option2Count = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer option3Count = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer option4Count = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalAttempts = 0;

    public void incrementOptionCount(int optionNumber) {
        switch (optionNumber) {
            case 1 -> option1Count++;
            case 2 -> option2Count++;
            case 3 -> option3Count++;
            case 4 -> option4Count++;
        }
        totalAttempts++;
    }

    public double getOption1Percentage() {
        return totalAttempts == 0 ? 0 : (option1Count * 100.0) / totalAttempts;
    }

    public double getOption2Percentage() {
        return totalAttempts == 0 ? 0 : (option2Count * 100.0) / totalAttempts;
    }

    public double getOption3Percentage() {
        return totalAttempts == 0 ? 0 : (option3Count * 100.0) / totalAttempts;
    }

    public double getOption4Percentage() {
        return totalAttempts == 0 ? 0 : (option4Count * 100.0) / totalAttempts;
    }
}
