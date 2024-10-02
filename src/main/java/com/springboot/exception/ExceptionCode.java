package com.springboot.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ExceptionCode {
    PARAM_NOT_FOUND(400, "At least 1 of valid params required"),
    DISCONTINUOUS_TIME(400, "Reservation times are discontinuous"),
    INVALID_CANCLE_REASON(400, "Invalid cancel reason"),
    LICENSE_AMOUNT_VIOLATION(400, "At least 1 and maximum 3 licences are required"),
    CAREER_AMOUNT_VIOLATION(400, "At least 1 and maximum 3 careers are required"),
    INVALID_USERTYPE(403, "Invalid usertype"),
    UNMATCHED_MEMBER(403, "Member id is not match"),
    UNMATCHED_COUNSELOR(403, "Counselor id is not match"),
    INVALID_COUNSELOR(403, "Invalid counselor"),
    UNCOMPLETE_COUNSELING(404, "Counseling not finished"),
    MEMBER_NOT_FOUND(404, "Member not found"),
    COUNSELOR_NOT_FOUND(404, "Counselor not found"),
    RESERVATION_NOT_FOUND(404, "Reservation not found"),
    UNAVAILABLE_DATE(404, "Reservation date is not available"),
    UNAVAILABLE_TIME(404, "Reservation time is not available"),
    DUPLICATED_USERID(409, "Duplicated userid"),
    DUPLICATED_NICKNAME(409, "Duplicated nickname"),
    SAME_PASSWORD(409, "The new password cannot be the same as the current one"),
    RESERVATION_TIMESLOT_OCCUPIED(409, "Already occupied timeslot"),
    REVIEW_EXIST(409, "Review already exists"),
    REPORT_EXIST(409, "Report already exists"),
    CREDENTIAL_NOT_FOUND(500, "Some credential field not found")
    ;
    @Getter
    private int status;
    @Getter
    private String message;
}
