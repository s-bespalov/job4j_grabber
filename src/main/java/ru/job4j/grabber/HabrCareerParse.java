package ru.job4j.grabber;

import org.jsoup.Jsoup;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PREFIX = "/vacancies?page=";
    private static final String SUFFIX = "&q=Java%20developer&type=all";

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) {
        var vacancies = new ArrayList<Post>();
        IntStream.rangeClosed(1, 5)
                .forEach(pageNumber -> {
                    var fullLink = "%s%s%d%s".formatted(link, PREFIX, pageNumber, SUFFIX);
                    try {
                        var connection = Jsoup.connect(fullLink);
                        var document = connection.get();
                        var rows = document.select(".vacancy-card__inner");
                        rows.forEach(row -> {
                            var titleElement = row.select(".vacancy-card__title").first();
                            var linkElement = Objects.requireNonNull(titleElement).child(0);
                            var vacancyName = titleElement.text();
                            var vacancyLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                            var dateElement = row.select(".vacancy-card__date").first();
                            var timeElement = Objects.requireNonNull(dateElement).child(0);
                            var timestamp = dateTimeParser.parse(timeElement.attr("datetime"));
                            var description = retrieveDescription(vacancyLink);
                            vacancies.add(new Post(vacancyName, vacancyLink, timestamp, description));
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        return vacancies;
    }

    private static String retrieveDescription(String link) {
        try {
            var connection = Jsoup.connect(link);
            var document = connection.get();
            var description = document.select(".vacancy-description__text").first();
            return Objects.requireNonNull(description).text();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) {
        var parser = new HabrCareerParse(new HabrCareerDateTimeParser());
        var vacancies = parser.list(HabrCareerParse.SOURCE_LINK);
        vacancies.forEach(System.out::println);
    }
}