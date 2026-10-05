package com.example.moviebooking.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevelopmentWhatsAppProvider implements WhatsAppProvider {
    private static final Logger log = LoggerFactory.getLogger(DevelopmentWhatsAppProvider.class);
    @Override public boolean isConfigured() { return true; }
    @Override public void send(String mobile, String message) {
        log.info("LOCAL DEVELOPMENT ONLY - WhatsApp to {}: {}", mobile, message);
    }
}
