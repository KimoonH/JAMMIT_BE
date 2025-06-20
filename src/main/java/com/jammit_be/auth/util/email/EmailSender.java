package com.jammit_be.auth.util.email;

public interface EmailSender {

    void sendEmail(String to, String subject, String content);
}
