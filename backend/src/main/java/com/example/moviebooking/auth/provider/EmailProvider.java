package com.example.moviebooking.auth.provider;

public interface EmailProvider {
    void send(String to, String subject, String body);
}
