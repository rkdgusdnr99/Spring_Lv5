package com.example.postlv3.dto;

import lombok.Getter;

@Getter
public class StatusResponseDto {

    private String msg;
    private Integer statusCode;

    public StatusResponseDto(String msg, Integer statusCode) {
        this.msg = msg;
        this.statusCode = statusCode;
    }
}