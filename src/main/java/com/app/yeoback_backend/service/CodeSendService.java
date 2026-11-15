package com.app.yeoback_backend.service;

import com.app.yeoback_backend.common.enumeration.CodePurpose;
import com.app.yeoback_backend.common.enumeration.ContactMethod;
import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 인증코드 실제 발송
 *  - 이메일: spring.mail (Gmail SMTP)
 *  - 문자:   SOLAPI (message.api-key / api-secret-key / sender-number)
 * 발송에 실패하면 IllegalStateException 을 던진다. 처리 방식은 VerificationService 가 정한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CodeSendService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;
    @Value("${message.api-key:}")
    private String smsApiKey;
    @Value("${message.api-secret-key:}")
    private String smsApiSecret;
    @Value("${message.sender-number:}")
    private String smsSender;

    private DefaultMessageService smsService;

    public void send(ContactMethod method, String contact, CodePurpose purpose, String code, long ttlMinutes) {
        String title = purpose == CodePurpose.SIGNUP ? "회원가입" : "비밀번호 재설정";
        String text = "[여백] " + title + " 인증코드는 " + code + " 입니다. (" + ttlMinutes + "분 이내 입력)";

        if (method == ContactMethod.EMAIL) {
            sendMail(contact, "[여백] " + title + " 인증코드", text);
        } else {
            sendSms(contact, text);
        }
    }

    private void sendMail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("verification mail sent");
        } catch (Exception e) {
            throw new IllegalStateException("메일 발송 실패: " + e.getMessage(), e);
        }
    }

    private void sendSms(String to, String text) {
        if (smsSender == null || smsSender.isBlank()) {
            throw new IllegalStateException("문자 발신번호(message.sender-number)가 설정되지 않았어요.");
        }
        try {
            Message message = new Message();
            message.setFrom(smsSender);
            message.setTo(to);
            message.setText(text);
            sms().send(message);
            log.info("verification sms sent");
        } catch (Exception e) {
            throw new IllegalStateException("문자 발송 실패: " + e.getMessage(), e);
        }
    }

    private synchronized DefaultMessageService sms() {
        if (smsService == null) {
            smsService = SolapiClient.INSTANCE.createInstance(smsApiKey, smsApiSecret);
        }
        return smsService;
    }
}
