package com.example.moviebooking.auth.entity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="otp_verifications")
public class OtpVerification {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,length=20) public String mobile;
 @Column(nullable=false) public String otpHash;
 @Column(nullable=false) public Instant expiresAt;
 @Column(nullable=false) public int attemptCount;
 @Column(nullable=false) public boolean verified;
 @Column(nullable=false) public Instant createdAt=Instant.now();
}
