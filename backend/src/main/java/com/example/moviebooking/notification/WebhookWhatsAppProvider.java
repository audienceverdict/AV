package com.example.moviebooking.notification;

import com.example.moviebooking.common.exception.ApiException;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

@Component
@Profile("!dev")
public class WebhookWhatsAppProvider implements WhatsAppProvider {
    private final RestClient client; private final String url, token;
    public WebhookWhatsAppProvider(@Value("${whatsapp.webhook-url:}") String url, @Value("${whatsapp.api-token:}") String token) {
        this.url=url; this.token=token;
        var factory=new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()); factory.setReadTimeout(Duration.ofSeconds(10));
        client=RestClient.builder().requestFactory(factory).build();
    }
    @Override public boolean isConfigured() { return !url.isBlank() && !token.isBlank(); }
    @Override public void send(String mobile,String message) {
        if (!isConfigured()) throw new ApiException(503,"WHATSAPP_NOT_CONFIGURED","WhatsApp delivery is not configured yet.");
        if (!url.startsWith("https://")) throw new ApiException(503,"WHATSAPP_UNAVAILABLE","WhatsApp webhook must use HTTPS.");
        try { client.post().uri(url).header("Authorization","Bearer "+token).body(Map.of("mobile",mobile,"message",message)).retrieve().toBodilessEntity(); }
        catch(Exception e) { throw new ApiException(503,"WHATSAPP_UNAVAILABLE","Unable to send WhatsApp message."); }
    }
}
