package com.chump.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender sender;
    private final String from = "noreply@example.com"; // TODO поместить в application.properties

    // Side (от side effect) в названии означает, что метод не пробрасывает исключение
    // т.к. используется по назначению побочного эффекта (при биллинге, компроментации...) и не должен
    // обрабатываться отдельно при падении
    @Async("emailTaskExecutor")
    public void asyncSideSendMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        try {
            sender.send(message);
            log.info("Email with subject '{}' sent to: {}", subject, to);
        } catch (MailException e) {
            log.error("Failed to sent message to {}", to, e);
        }
    }
}
