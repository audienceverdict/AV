package com.example.moviebooking.auth.controller;
import com.example.moviebooking.auth.dto.*;
import com.example.moviebooking.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
@RestController @RequestMapping("/api/v1/auth") public class AuthController {
 private final AuthService service;public AuthController(AuthService service){this.service=service;}
 @PostMapping("/email-otp/request") public OtpResponse requestEmail(@Valid @RequestBody EmailOtpRequest r){return service.requestEmail(r);}
 @PostMapping("/email-otp/verify") public EmailOtpResult verifyEmail(@Valid @RequestBody EmailOtpVerifyRequest r){return service.verifyEmail(r);}
 @PostMapping("/email-otp/register") public EmailOtpResult registerEmail(@Valid @RequestBody EmailOtpRegistrationRequest r){return service.registerEmail(r);}
 @GetMapping("/me") public UserResponse me(Principal p){return service.current(p.getName());}
 @PutMapping("/me") public UserResponse update(Principal p,@Valid @RequestBody UpdateProfileRequest r){return service.update(p.getName(),r);}
}
