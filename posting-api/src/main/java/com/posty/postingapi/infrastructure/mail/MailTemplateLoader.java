package com.posty.postingapi.infrastructure.mail;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class MailTemplateLoader {

    private final ResourceLoader resourceLoader;

    public MailTemplateLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public MailTemplate load(String path) {
        Resource resource = resourceLoader.getResource("classpath:" + path);

        String html;
        try (InputStream is = resource.getInputStream()) {
            html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load email template for verification code...", e);
        }

        int titleStart = html.indexOf("<title>");
        int titleEnd = html.indexOf("</title>");
        if (titleStart == -1 || titleEnd == -1) {
            throw new IllegalStateException("Email template must contain <title>");
        }
        String title = html.substring(titleStart + 7, titleEnd).trim();

        int bodyStart = html.indexOf("<body>");
        int bodyEnd = html.indexOf("</body>");
        if (bodyStart == -1 || bodyEnd == -1) {
            throw new IllegalStateException("Email template must contain <body>");
        }
        String body = html.substring(bodyStart + 6, bodyEnd).trim();

        return new MailTemplate(title, body);
    }

    public MailTemplate loadVerificationCodeTemplate() {
        return load("templates/verification-code-email.html");
    }
}
