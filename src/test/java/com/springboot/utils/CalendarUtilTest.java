package com.springboot.utils;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CalendarUtilTest {

    @Test
    void getMonthDates() {
        // given
        Map<YearMonth,Integer> given = new HashMap<>(){{
            put(YearMonth.of(2024, 1), 31);
            put(YearMonth.of(2024, 2), 29);
            put(YearMonth.of(2024, 3), 31);
            put(YearMonth.of(2024, 4), 30);
            put(YearMonth.of(2024, 5), 31);
            put(YearMonth.of(2024, 6), 30);
            put(YearMonth.of(2024, 7), 31);
            put(YearMonth.of(2024, 8), 31);
            put(YearMonth.of(2024, 9), 30);
            put(YearMonth.of(2024, 10), 31);
            put(YearMonth.of(2024, 11), 30);
            put(YearMonth.of(2024, 12), 31);
        }};

        // when, then
        for(YearMonth month : given.keySet()){
            int year = month.getYear();
            int monthValue = month.getMonthValue();

            int expected = given.get(month);
            List<LocalDate> actual = CalendarUtil.getMonthDates(month);

            assertEquals(expected, actual.size(), String.format("%d년 %d월은 %d개의 날짜가 있어야 합니다.", year, monthValue, expected));
            assertEquals(1, actual.get(0).getDayOfMonth(), "결과 리스트의 첫 날짜는 1일이어야 합니다.");
            assertEquals(expected, actual.get(actual.size() - 1).getDayOfMonth(), String.format("%d년 %d월의 마지막 날짜는 %d일이어야 합니다.", year, monthValue, expected));
        }
    }

    @Test
    void testGetNextDateOfCertainDayOfWeek() {
        // given
        LocalDate givenDate = LocalDate.of(2024, 11, 29);

        Map<DayOfWeek, LocalDate> givenDayOfWeeks = Map.of(
                DayOfWeek.MONDAY, LocalDate.of(2024, 12, 2),
                DayOfWeek.TUESDAY, LocalDate.of(2024, 12, 3),
                DayOfWeek.WEDNESDAY, LocalDate.of(2024, 12, 4),
                DayOfWeek.THURSDAY, LocalDate.of(2024, 12, 5),
                DayOfWeek.FRIDAY, LocalDate.of(2024, 11, 29),
                DayOfWeek.SATURDAY, LocalDate.of(2024, 11, 30),
                DayOfWeek.SUNDAY, LocalDate.of(2024, 12, 1)
        );


        // when, then
        for(DayOfWeek dayOfWeek : givenDayOfWeeks.keySet()){
            LocalDate expected = givenDayOfWeeks.get(dayOfWeek);
            LocalDate actual = CalendarUtil.getNextDateOfCertainDayOfWeek(dayOfWeek, givenDate);

            assertEquals(expected, actual, String.format("%s 와 가장 가까운 다음 %s는 %s 여야 합니다",
                    givenDate.toString(), dayOfWeek.toString(), givenDayOfWeeks.get(dayOfWeek).toString()));
        }
    }

    @Test
    void isLocalDateInYearMonth() {
        // given
        YearMonth givenMonth = YearMonth.of(2024, 11);
        Map<LocalDate, Boolean> givenDates = Map.of(
                LocalDate.of(2024, 7, 14), false,
                LocalDate.of(2024, 11, 2), true,
                LocalDate.of(2024, 11, 17), true,
                LocalDate.of(2024, 4, 20), false,
                LocalDate.of(2024, 6, 8), false,
                LocalDate.of(2024, 11, 25), true,
                LocalDate.of(2024, 1, 13), false,
                LocalDate.of(2024, 9, 12), false,
                LocalDate.of(2024, 11, 6), true,
                LocalDate.of(2024, 10, 18), false
        );

        // when, then
        for(LocalDate givenDate : givenDates.keySet()){
            boolean expected = givenDates.get(givenDate);
            boolean actual = CalendarUtil.isLocalDateInYearMonth(givenDate, givenMonth);

            assertEquals(expected, actual, String.format("%s 은(는) %d년 %d월에 ", givenDate, givenMonth.getYear(), givenMonth.getMonthValue())
                    + (expected ? "속해야" : "속하지 않아야") + " 합니다.");
        }
    }
}