package com.example.moviebooking.notification;

public interface WhatsAppProvider {
    boolean isConfigured();
    void send(String mobile, String message);
}
