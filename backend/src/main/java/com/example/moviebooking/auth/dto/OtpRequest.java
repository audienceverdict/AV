package com.example.moviebooking.auth.dto;
import jakarta.validation.constraints.*;
public record OtpRequest(@NotBlank String mobile) {}
