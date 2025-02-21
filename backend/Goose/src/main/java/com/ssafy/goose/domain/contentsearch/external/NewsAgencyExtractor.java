package com.ssafy.goose.domain.contentsearch.external;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class NewsAgencyExtractor {
    public String extractNewsAgency(String newsUrl) {
        try {
            if (newsUrl == null || newsUrl.isEmpty()) {
                return "Unknown";
            }

            String estimatedAgency = estimateNewsAgencyFromUrl(newsUrl);
            if (!estimatedAgency.equals("Unknown")) {
                return estimatedAgency;
            }

            Document doc = Jsoup.connect(newsUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .header("Referer", "https://www.naver.com/")
                    .timeout(5000)
                    .get();

            Element logoImg = doc.selectFirst("img.media_end_head_top_logo_img");
            if (logoImg != null) {
                String newsAgency = logoImg.attr("title").trim();
                if (!newsAgency.isEmpty()) {
                    return newsAgency;
                }
            }

            Element metaSiteName = doc.selectFirst("meta[property=og:site_name]");
            if (metaSiteName != null) {
                String content = metaSiteName.attr("content").trim();
                if (!content.isEmpty()) {
                    return content;
                }
            }
        } catch (Exception e) {
            // 크롤링 실패 시 도메인 추정 로직으로 넘어감
        }
        return estimateNewsAgencyFromUrl(newsUrl);
    }

    private String estimateNewsAgencyFromUrl(String newsUrl) {
        if (newsUrl.contains("jtbc")) return "JTBC 뉴스";
        if (newsUrl.contains("kmib")) return "국민일보";
        if (newsUrl.contains("joongang")) return "중앙일보";
        if (newsUrl.contains("kado")) return "강원도민일보";
        if (newsUrl.contains("dailian")) return "데일리안";
        if (newsUrl.contains("tf")) return "더팩트";
        if (newsUrl.contains("lecturernews")) return "한국강사신문";
        if (newsUrl.contains("news1")) return "뉴스1";
        if (newsUrl.contains("newsen")) return "뉴스엔";
        if (newsUrl.contains("kbs")) return "KBS 뉴스";
        if (newsUrl.contains("sbs")) return "SBS 뉴스";
        if (newsUrl.contains("mbc")) return "MBC 뉴스";
        if (newsUrl.contains("ytn")) return "YTN";
        if (newsUrl.contains("chosun")) return "조선일보";
        if (newsUrl.contains("hani")) return "한겨레";
        if (newsUrl.contains("yna")) return "연합뉴스";
        if (newsUrl.contains("cnb")) return "CNB 뉴스";
        if (newsUrl.contains("cwn")) return "CWN 뉴스";
        if (newsUrl.contains("siminilbo")) return "시민일보";
        if (newsUrl.contains("youthdaily")) return "유스데일리";
        if (newsUrl.contains("youngnong")) return "한국영농신문";
        if (newsUrl.contains("autotimes")) return "오토타임즈";
        if (newsUrl.contains("tjb")) return "TJB 뉴스";
        if (newsUrl.contains("thefirstmedia")) return "더퍼스트미디어";
        if (newsUrl.contains("edaily")) return "이데일리";
        if (newsUrl.contains("cpbc")) return "가톨릭평화방송";
        if (newsUrl.contains("mbn")) return "MBN 매일방송";
        return "Unknown";
    }
}
