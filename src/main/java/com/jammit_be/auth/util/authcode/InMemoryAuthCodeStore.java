package com.jammit_be.auth.util.authcode;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryAuthCodeStore implements AuthCodeStore {

    private final Map<String, AuthCodeInfo> store = new ConcurrentHashMap<>();

    private static class AuthCodeInfo {
        final String code;
        final Instant expireAt;

        public AuthCodeInfo(String code, Instant expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }

    @Override
    public void saveCode(String email, String code, int expireSeconds) {
        store.put(email, new AuthCodeInfo(code, Instant.now().plusSeconds(expireSeconds)));
    }

    @Override
    public AuthCodeVerifyResult verifyCode(String email, String code) {
        AuthCodeInfo info = store.get(email);
        if (info == null) {
            return AuthCodeVerifyResult.NOT_FOUND;
        }
        if (Instant.now().isAfter(info.expireAt)) {
            store.remove(email);
            return AuthCodeVerifyResult.EXPIRED;
        }
        if (info.code.equals(code)) {
            store.remove(email); // 인증 성공 시 삭제
            return AuthCodeVerifyResult.SUCCESS;
        } else {
            return AuthCodeVerifyResult.INVALID;
        }
    }

    @Override
    public boolean hasCode(String email) {
        return store.containsKey(email);
    }
}
