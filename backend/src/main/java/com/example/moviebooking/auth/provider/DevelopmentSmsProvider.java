package com.example.moviebooking.auth.provider;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;
import org.slf4j.*;
@Component @Profile("dev") public class DevelopmentSmsProvider implements SmsProvider {
 private static final Logger log=LoggerFactory.getLogger(DevelopmentSmsProvider.class);
 public void sendOtp(String mobile,String otp){log.info("LOCAL DEVELOPMENT ONLY - OTP for {}: {}",mobile,otp);}
}
