package com.springboot.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CalendarUtil {

    // 특정 달에 속한 전체 일 반환
    public static List<LocalDate> getMonthDates(YearMonth month){
        LocalDate lastDate = month.atEndOfMonth();
        int lastDateNumber = lastDate.getDayOfMonth();

        // 첫 날부터 마지막 날까지 리스트에 담기
        List<LocalDate> dates = new ArrayList<>();
        for(int i = 1; i<= lastDateNumber; i++){
            dates.add(LocalDate.of(month.getYear(), month.getMonthValue(), i));
        }
        return dates;
    }

    // 요일을 입력받아 해당 요일 중 오늘로부터 가장 가까운 날짜 반환
    public static LocalDate getNextDateOfCertainDayOfWeek(DayOfWeek dayOfWeek) {
        LocalDate today = LocalDate.now();
        int daysUntilNext = (dayOfWeek.getValue() - today.getDayOfWeek().getValue() + 7) % 7;

        return today.plusDays(daysUntilNext);
    }
    // 요일을 입력받아 해당 요일 중 기준일로부터 가장 가까운 날짜 반환
    public static LocalDate getNextDateOfCertainDayOfWeek(DayOfWeek dayOfWeek, LocalDate refDate) {
        LocalDate today = LocalDate.now();
        int daysUntilNext = (dayOfWeek.getValue() - today.getDayOfWeek().getValue() + 7) % 7;

        return today.plusDays(daysUntilNext);
    }
}
