package com.example.moviebooking.auth.service;
import com.example.moviebooking.auth.entity.*;
import com.example.moviebooking.auth.repository.*;
import com.example.moviebooking.auth.provider.SmsProvider;
import com.example.moviebooking.auth.dto.OtpResponse;
import com.example.moviebooking.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import java.security.SecureRandom;
import java.time.Instant;
@Service public class OtpService {
 private final OtpVerificationRepository records; private final AuthLockRepository locks; private final SmsProvider sms; private final com.example.moviebooking.auth.provider.EmailProvider email;
 private final BCryptPasswordEncoder hashes=new BCryptPasswordEncoder(); private final SecureRandom random=new SecureRandom();
 private final long expiry,cooldown; private final int attempts,requests;
 public OtpService(OtpVerificationRepository records,AuthLockRepository locks,SmsProvider sms,com.example.moviebooking.auth.provider.EmailProvider email,
 @Value("${auth.otp.expiration-seconds:300}") long expiry,@Value("${auth.otp.resend-cooldown-seconds:30}") long cooldown,
 @Value("${auth.otp.max-attempts:5}") int attempts,@Value("${auth.otp.max-requests-per-hour:10}") int requests){
 this.records=records;this.locks=locks;this.sms=sms;this.email=email;this.expiry=expiry;this.cooldown=cooldown;this.attempts=attempts;this.requests=requests;
 if(expiry<1||cooldown<1||attempts<1||requests<1)throw new IllegalArgumentException("OTP limits must be positive");}
 public void lock(String mobile){locks.lock(Math.floorMod(mobile.hashCode(),64));}
 @Transactional public OtpResponse request(String mobile){
 return requestCode(mobile,"SMS");
 }
 @Transactional public OtpResponse requestEmail(String address){ return requestCode(address,"EMAIL"); }
 private OtpResponse requestCode(String destination,String channel){
 lock(destination);Instant now=Instant.now();
 var latest=records.findFirstByMobileAndChannelOrderByIdDesc(destination,channel);
 if(latest.isPresent()&&latest.get().createdAt.plusSeconds(cooldown).isAfter(now))throw new ApiException(429,"OTP_COOLDOWN","Please wait before requesting another code");
 if(records.countByMobileAndChannelAndCreatedAtAfter(destination,channel,now.minusSeconds(3600))>=requests)throw new ApiException(429,"OTP_RATE_LIMIT","Too many requests. Please try again later.");
 String code=String.format("%06d",random.nextInt(1000000));
 OtpVerification otp=new OtpVerification();otp.mobile=destination;otp.channel=channel;otp.otpHash=hashes.encode(code);otp.createdAt=now;otp.expiresAt=now.plusSeconds(expiry);
 records.saveAndFlush(otp);if(channel.equals("EMAIL"))email.send(destination,"Your Audience Verdict sign-in code","Your verification code is "+code+". It expires in "+(expiry/60)+" minutes.");else sms.sendOtp(destination,code);
 return new OtpResponse(true,"Verification code sent",expiry,cooldown);
 }
 // Called inside AuthService's transaction. Invalid attempts must commit as well.
 public void verify(String mobile,String code){
 verify(mobile,code,"SMS");
 }
 public void verifyEmail(String address,String code){ verify(address,code,"EMAIL"); }
 private void verify(String destination,String code,String channel){
 lock(destination);var otp=records.findFirstByMobileAndChannelOrderByIdDesc(destination,channel).orElseThrow(()->invalid());
 if(otp.verified||!otp.expiresAt.isAfter(Instant.now()))throw invalid();
 if(otp.attemptCount>=attempts)throw new ApiException(429,"OTP_ATTEMPT_LIMIT","Too many attempts. Request a new code.");
 otp.attemptCount++;records.save(otp);
 if(!hashes.matches(code,otp.otpHash))throw invalid();
 otp.verified=true;records.save(otp);
 }
 private ApiException invalid(){return new ApiException(400,"INVALID_OTP","The code is invalid or expired");}
 // Keep request history beyond the hourly rate-limit window, even for expired codes.
 @Scheduled(fixedDelay=3600000) @Transactional public void cleanup(){records.deleteByCreatedAtBefore(Instant.now().minusSeconds(Math.max(86400,expiry+3600)));}
}
