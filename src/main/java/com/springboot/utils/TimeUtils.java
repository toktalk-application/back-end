package com.springboot.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.units.qual.A;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TimeUtils {

    public static TimeComparisonResult compare(List<LocalTime> currentTimes, List<LocalTime> newTimes){

        Set<LocalTime> additions = new HashSet<>(newTimes);     // currentTimes에는 없고, newTimes에는 있음
        Set<LocalTime> unchanged = new HashSet<>();             // 양쪽에 다 있음
        Set<LocalTime> removes = new HashSet<>(currentTimes);   // currentTimes에는 있고, newTimes에는 없음

        // 각각을 순회하면서 양쪽에 다 있는 경우에만 unchanged에 추가
        for (LocalTime time : currentTimes) {
            if (newTimes.contains(time)) {                      // 순회가 끝나면 최종적으로
                unchanged.add(time);                            // unchanged = unchanged
                additions.remove(time);                         // additions = newTimes - unchanged
                removes.remove(time);                           // removes = currentTimes - unchanged 가 됨
            }
        }
        return new TimeComparisonResult(additions, unchanged, removes);
    }
    @Getter
    @Setter
    @AllArgsConstructor
    public static class TimeComparisonResult{
        private Set<LocalTime> additions;
        private Set<LocalTime> unchanged;
        private Set<LocalTime> removed;
    }

    public static boolean isPassedTime(LocalDateTime time){
        return time.isEqual(LocalDateTime.now()) || time.isBefore(LocalDateTime.now());
    }
}
