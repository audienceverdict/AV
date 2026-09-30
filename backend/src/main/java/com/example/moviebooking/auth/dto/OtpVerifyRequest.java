package com.example.moviebooking.auth.dto;
import jakarta.validation.constraints.*;
public record OtpVerifyRequest(@NotBlank String mobile, @NotBlank @Pattern(regexp="[0-9]{6}") String otp) {}
