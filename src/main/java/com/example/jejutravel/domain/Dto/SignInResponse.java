package com.example.jejutravel.domain.Dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SignInResponse {

    private String userName;
    private String msg;
    private String accessToken;
    private String refreshToken;

    @Builder
    public SignInResponse(String userName, String msg, String accessToken, String refreshToken) {
        this.userName = userName;
        this.msg = msg;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
    
}
