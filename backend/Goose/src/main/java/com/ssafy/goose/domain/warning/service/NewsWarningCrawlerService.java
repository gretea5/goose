package com.ssafy.goose.domain.warning.service;

import com.ssafy.goose.domain.warning.entity.WarningNewsAgency;
import com.ssafy.goose.domain.warning.repository.WarningNewsAgencyRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

@Service
public class NewsWarningCrawlerService {

    private final WarningNewsAgencyRepository warningNewsAgencyRepository;
    private static final String BASE_URL = "https://news.naver.com/main/ombudsman/revisionArticleList.naver?mode=LSD&mid=omb&page=";

    public NewsWarningCrawlerService(WarningNewsAgencyRepository warningNewsAgencyRepository) {
        this.warningNewsAgencyRepository = warningNewsAgencyRepository;
    }

    @Transactional
    public void crawlAndSaveNewsWarnings() {
        Map<String, Integer> agencyCountMap = new HashMap<>();

        try {
            for (int page = 1; page <= 41; page++) {
                String url = BASE_URL + page;
                Document doc = Jsoup.connect(url).get();

                Elements agencyElements = doc.select("span.writing");

                for (Element agencyElement : agencyElements) {
                    String newsAgency = agencyElement.text().trim();

                    agencyCountMap.put(newsAgency, agencyCountMap.getOrDefault(newsAgency, 0) + 1);
                }
            }

            saveDataToDatabase(agencyCountMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public void saveDataToDatabase(Map<String, Integer> agencyCountMap) {
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(agencyCountMap.entrySet());
        sortedList.sort((a, b) -> b.getValue().compareTo(a.getValue())); // 기사 개수 기준 내림차순 정렬

        int rank = 1;
        for (Map.Entry<String, Integer> entry : sortedList) {
            WarningNewsAgency agency = warningNewsAgencyRepository.findByNewsAgency(entry.getKey());

            if (agency == null) {
                agency = new WarningNewsAgency();
                agency.setNewsAgency(entry.getKey());
            }

            agency.setWarningCount(entry.getValue());
            agency.setRanking(rank++);
            warningNewsAgencyRepository.save(agency);
        }
    }
}
