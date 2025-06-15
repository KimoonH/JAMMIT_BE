package com.jammit_be.auth.service;

import com.jammit_be.auth.dto.response.EmailVerifyCodeResponse;
import com.jammit_be.auth.util.authcode.AuthCodeStore;
import com.jammit_be.auth.util.authcode.AuthCodeVerifyResult;
import com.jammit_be.auth.util.email.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailAuthService {

    private final AuthCodeStore authCodeStore;
    private final EmailSender emailSender;

    // 인증 번호 전송
    public void sendAuthCode(String email) {
        String code = generateRandomCode();
        int expireSeconds = 180; // 3분 유효

        authCodeStore.saveCode(email, code, expireSeconds);
        emailSender.sendEmail(
                email,
                "[JAMMIT] 이메일 인증번호 안내",
                "인증번호: <b>" + code + "</b> (3분 이내 입력)"
        );
    }

    public AuthCodeVerifyResult verifyAuthCode(String email, String code) {
        return authCodeStore.verifyCode(email, code);
    }

    private String generateRandomCode() {
        Random random = new Random();
        int num = random.nextInt(900_000) + 100_000; // 6자리
        return String.valueOf(num);
    }
}
