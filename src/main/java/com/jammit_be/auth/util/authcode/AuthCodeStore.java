package com.jammit_be.auth.util.authcode;

public interface AuthCodeStore {

    // 인증 번호 저장(이메일, 인증코드, 만료시간(초)
    void saveCode(String email, String code, int expireSeconds);

    // 인증번호 검증 및 1회성 삭제
    AuthCodeVerifyResult verifyCode(String email, String code);

    // 인증 번호 존재 여부
    boolean hasCode(String email);
}
