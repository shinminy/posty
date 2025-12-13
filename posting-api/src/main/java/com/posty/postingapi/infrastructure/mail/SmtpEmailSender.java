package com.posty.postingapi.infrastructure.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class SmtpEmailSender {

    private final JavaMailSender mailSender;

    private final String noReplyFrom;

    public SmtpEmailSender(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;

        noReplyFrom = mailProperties.getFrom().getNoReply();
    }

    public void sendHtml(String to, String subject, String htmlBody) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setFrom(noReplyFrom);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to setting MimeMessageHelper...", e);
        }

        mailSender.send(message);
    }
}
