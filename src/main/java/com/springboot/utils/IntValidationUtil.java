package com.springboot.utils;

public class IntValidationUtil {
    public static boolean isIntInRange(int i, int from, int to){
        return i >= from && i <= to;
    }
}
