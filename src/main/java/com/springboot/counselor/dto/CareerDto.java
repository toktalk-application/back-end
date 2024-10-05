package com.springboot.counselor.dto;

import com.springboot.counselor.entity.Career;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


public class CareerDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Post{
        private Career.Classification classification;
        private String company;
        private String responsibility;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response{
        private long careerId;
        private Career.Classification classification;
        private String company;
        private String responsibility;
    }
}
