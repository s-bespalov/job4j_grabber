package ru.job4j.grabber;

import org.jsoup.Jsoup;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.IntStream;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PREFIX = "/vacancies?page=";
    private static final String SUFFIX = "&q=Java%20developer&type=all";

    private static final DateTimeParser DATE_TIME_PARSER = new HabrCareerDateTimeParser();

    public static void main(String[] args) {
        var vacancies = new ArrayList<Vacancy>();
        IntStream.rangeClosed(1, 5)
                .forEach(pageNumber -> {
                    var fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
                    try {
                        var connection = Jsoup.connect(fullLink);
                        var document = connection.get();
                        var rows = document.select(".vacancy-card__inner");
                        rows.forEach(row -> {
                            var titleElement = row.select(".vacancy-card__title").first();
                            var linkElement = Objects.requireNonNull(titleElement).child(0);
                            var vacancyName = titleElement.text();
                            var link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                            var dateElement = row.select(".vacancy-card__date").first();
                            var timeElement = Objects.requireNonNull(dateElement).child(0);
                            var timestamp = DATE_TIME_PARSER.parse(timeElement.attr("datetime"));
                            vacancies.add(new Vacancy(vacancyName, link, timestamp));
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        vacancies.forEach(System.out::println);
        System.out.println(vacancies.size());
    }

    private record Vacancy(String name, String link, LocalDateTime date) {
    }
}