package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class HabrCareerDateTimeParserTest {

    private static final DateTimeParser DATE_TIME_PARSER = new HabrCareerDateTimeParser();

    @Test
    public void whenFirstJanuary2022() {
        var result = DATE_TIME_PARSER.parse("2022-01-01T00:00:00+00:00");
        var expected = "2022-01-01T00:00:00";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void whenEightMart2023() {
        var result = DATE_TIME_PARSER.parse("2023-03-08T00:00:00+03:00");
        var expected = "2023-03-08T00:00:00";
        assertThat(result).isEqualTo(expected);
    }

}