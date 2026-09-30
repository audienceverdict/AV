package com.example.moviebooking.auth.provider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import com.example.moviebooking.common.exception.ApiException;
import java.util.Map;
@Component @Profile("!dev") public class WebhookSmsProvider implements SmsProvider {
 private final RestClient client; private final String url,token;
 public WebhookSmsProvider(@Value("${sms.webhook-url:}") String url,@Value("${sms.api-token:}") String token){this.url=url;this.token=token;var factory=new org.springframework.http.client.JdkClientHttpRequestFactory(java.net.http.HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(5)).build());factory.setReadTimeout(java.time.Duration.ofSeconds(10));this.client=RestClient.builder().requestFactory(factory).build();if(!url.startsWith("https://")||token.isBlank())throw new IllegalStateException("Production requires HTTPS SMS_WEBHOOK_URL and SMS_API_TOKEN");}
 public void sendOtp(String mobile,String otp){try{client.post().uri(url).header("Authorization","Bearer "+token).body(Map.of("mobile",mobile,"message","Your Audience Verdict verification code is "+otp)).retrieve().toBodilessEntity();}catch(Exception e){throw new ApiException(503,"SMS_UNAVAILABLE","Unable to send code. Please try again later.");}}
}
