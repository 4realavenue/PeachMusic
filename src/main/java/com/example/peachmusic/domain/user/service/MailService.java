package com.example.peachmusic.domain.user.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.user.component.RedisEmailVerificationConfig;
import com.example.peachmusic.domain.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


@Service
@RequiredArgsConstructor
public class MailService {

    private final RedisEmailVerificationConfig verificationStore;
    private final UserRepository userRepository;
    private final SpringTemplateEngine templateEngine;

    @Autowired
    private final JavaMailSender javaMailSender;

    /**
     *  이메일 보내기
     */
    public void sendEmail(String toEmail, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        javaMailSender.send(message);
    }

    /**
     *  코드 전송
     */
    public void sendCodeToEmail(String email) {

        if (isEmailRegistered(email)) {
            throw new CustomException(ErrorCode.USER_EXIST_EMAIL);
        }

        String authCode = generateAuthCode();
        verificationStore.saveVerificationCode(email, authCode);

        Context context = new Context();
        context.setVariable("authCode", authCode);

        String emailContent = templateEngine.process("MailTemplate", context);

        try {
        sendEmail(email, "[PeachMusic] 이메일 인증 코드", emailContent);
        } catch (MessagingException e) {
            throw new CustomException(ErrorCode.USER_EXIST_EMAIL);
        }
    }


    /**
     *  코드 일치 여부 확인
     */
    public CommonResponse<Void> verifyEmailCode(String email, String code) {
        String savedCode = verificationStore.getVerificationCode(email);

        if (savedCode == null) {
            throw new CustomException(ErrorCode.AUTH_CODE_EXPIRED);
        }
        if (!savedCode.equals(code)) {
            throw new CustomException(ErrorCode.AUTH_CODE_INVALID);
        }
        verificationStore.removeVerificationCode(email);
        verificationStore.verifyEmail(email);
        return CommonResponse.success("이메일 인증이 완료되었습니다.");
    }

    /**
     *  이메일인증 체크
     */
    public boolean isEmailVerified(String email) {
        return verificationStore.isEmailVerified(email);
    }

    /**
     *  코드 생성
     */
    private String generateAuthCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }

    /**
     * 중복 체크
     */
    public boolean isEmailRegistered(String email) {
        return userRepository.existsByEmail(email);
    }

}
