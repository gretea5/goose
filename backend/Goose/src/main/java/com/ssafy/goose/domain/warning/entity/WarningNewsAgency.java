package com.ssafy.goose.domain.warning.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "warning_news_agency")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WarningNewsAgency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String newsAgency; // 불공정 선거보도 경고&주의를 받은 언론사

    @Column(nullable = false)
    private int warningCount;  // 경고&주의를 받은 횟수

    @Column(nullable = false)
    private int ranking;       // 경고&주의를 받은 횟수 랭킹
}
