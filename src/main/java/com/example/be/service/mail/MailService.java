package com.example.be.service.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.from-name:MariaStore}")
    private String fromName;

    public void sendResetPasswordEmail(String toEmail, String resetLink) {
        String subject = "Reset your password";
        String html = """
            <div style="font-family:Arial,sans-serif;line-height:1.5">
              <h2>Password reset request</h2>
              <p>Click the button below to reset your password:</p>
              <p>
                <a href="%s"
                   style="display:inline-block;padding:10px 14px;text-decoration:none;border-radius:6px;background:#111;color:#fff">
                   Reset Password
                </a>
              </p>
              <p>If the button doesn't work, copy this link:</p>
              <p><a href="%s">%s</a></p>
              <hr/>
              <p style="color:#666;font-size:12px">If you didn't request this, ignore this email.</p>
            </div>
            """.formatted(resetLink, resetLink, resetLink);

        sendHtml(toEmail, subject, html);
    }

    private void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendVerifyEmail(String toEmail, String verifyLink) {
        String subject = "Verify your email";
        String html = """
        <div style="font-family:Arial,sans-serif;line-height:1.5">
          <h2>Email verification</h2>
          <p>Please verify your email address by clicking the button below:</p>
          <p>
            <a href="%s"
               style="display:inline-block;padding:10px 14px;text-decoration:none;border-radius:6px;background:#111;color:#fff">
               Verify Email
            </a>
          </p>
          <p>If the button doesn't work, copy this link:</p>
          <p><a href="%s">%s</a></p>
          <hr/>
          <p style="color:#666;font-size:12px">If you didn't create an account, ignore this email.</p>
        </div>
        """.formatted(verifyLink, verifyLink, verifyLink);

        sendHtml(toEmail, subject, html);
    }

}
