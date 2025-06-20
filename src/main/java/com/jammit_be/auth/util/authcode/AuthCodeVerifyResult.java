package com.jammit_be.auth.util.authcode;

public enum AuthCodeVerifyResult {

    SUCCESS("인증이 성공했습니다."),
    EXPIRED("인증번호가 만료되었습니다."),
    INVALID("인증번호가 일치하지 않습니다."),
    NOT_FOUND("해당 이메일에 인증 요청이 없습니다.");

    private final String message;

    AuthCodeVerifyResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
