package com.example.moviebooking.auth.dto;
import jakarta.validation.constraints.*;
public record UpdateProfileRequest(@NotBlank @Size(max=150) String name, @Email @Size(max=255) String email) {}
