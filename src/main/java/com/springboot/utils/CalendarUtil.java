package com.springboot.utils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
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
}
