package com.springboot.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@Setter
public class SingleResponseEntity<T> extends ResponseEntity<T> {
    private T data;
    public SingleResponseEntity(T data, HttpStatus httpStatus){
        super(data, httpStatus);
        this.data = data;
    }
}
