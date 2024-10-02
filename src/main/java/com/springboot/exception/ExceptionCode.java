package com.springboot.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ExceptionCode {
    PARAM_NOT_FOUND(400, "At least 1 of valid params required"),
    INVALID_USERTYPE(403, "Invalid usertype"),
    MEMBER_NOT_FOUND(404, "Member not found"),
    COUNSELOR_NOT_FOUND(404, "Counselor not found"),
    RESERVATION_NOT_FOUND(404, "Reservation not found"),
    UNAVAILABLE_DATE(404, "Reservation date is not available"),
    UNAVAILABLE_TIME(404, "Reservation time is not available"),
    DUPLICATED_USERID(409, "Duplicated userid"),
    DUPLICATED_NICKNAME(409, "Duplicated nickname"),
    SAME_PASSWORD(409, "The new password cannot be the same as the current one"),
    RESERVATION_TIMESLOT_OCCUPIED(409, "Already occupied timeslot"),
    ;
    @Getter
    private int status;
    @Getter
    private String message;
}
