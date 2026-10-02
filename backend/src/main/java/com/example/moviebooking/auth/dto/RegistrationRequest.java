package com.example.moviebooking.auth.dto;
import jakarta.validation.constraints.*;
public record RegistrationRequest(@NotBlank String name,@Email String email,@Size(max=20) String alternativeMobile) {}
