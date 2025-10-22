package com.jobplatform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void send(String to, String subject, String text) {
        if (!mailEnabled) {
            System.out.println("[MAIL DISABLED] To: " + to + " | Subject: " + subject + "\n" + text);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (MailException ex) {
            // Avoid crashing business flow in dev; log and continue
            System.err.println("[MAIL ERROR] " + ex.getMessage());
        }
    }

    public void sendTemplate(String to, String subject, String template, Context context) {
        if (!mailEnabled) {
            String body = templateEngine.process(template, context);
            System.out.println("[MAIL DISABLED][HTML] To: " + to + " | Subject: " + subject + "\n" + body);
            return;
        }
        try {
            String body = templateEngine.process(template, context);
            var mimeMessage = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            // Fallback to plain text
            send(to, subject, "(HTML email)\n\n" + templateEngine.process(template, context));
        }
    }
}
