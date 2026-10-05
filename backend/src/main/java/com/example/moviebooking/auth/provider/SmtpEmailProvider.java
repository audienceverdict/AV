package com.example.moviebooking.auth.provider;

import com.example.moviebooking.common.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SmtpEmailProvider implements EmailProvider {
    private static final Logger log=LoggerFactory.getLogger(SmtpEmailProvider.class);
    private final JavaMailSender mail;
    private final String from,host;private final boolean development;
    public SmtpEmailProvider(JavaMailSender mail, Environment environment,@Value("${notifications.email-from:}") String from,@Value("${spring.mail.host:}") String host) {
        this.mail = mail; this.from = from;this.host=host;this.development=environment.acceptsProfiles(Profiles.of("dev"));
        if (!development&&(from.isBlank()||host.isBlank())) throw new IllegalStateException("Production requires SMTP_HOST and NOTIFICATION_EMAIL_FROM");
    }
    @Override public void send(String to, String subject, String body) {
        if(host.isBlank()&&development){log.info("LOCAL DEVELOPMENT ONLY - Email to {} | {} | {}",to,subject,body);return;}
        try {
            var message = new SimpleMailMessage(); message.setFrom(from); message.setTo(to);
            message.setSubject(subject); message.setText(body); mail.send(message);
        } catch (Exception e) {
            Throwable cause = e;
            while (cause.getCause() != null && cause.getCause() != cause) cause = cause.getCause();
            log.warn("SMTP delivery failed for host {} (cause type: {})", host, cause.getClass().getSimpleName());
            throw new ApiException(503, "EMAIL_UNAVAILABLE", "Unable to send email. Please try again later.");
        }
    }
}
