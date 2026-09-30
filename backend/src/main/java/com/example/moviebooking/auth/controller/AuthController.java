package com.example.moviebooking.auth.controller;
import com.example.moviebooking.auth.dto.*;
import com.example.moviebooking.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
@RestController @RequestMapping("/api/v1/auth") public class AuthController {
 private final AuthService service;public AuthController(AuthService service){this.service=service;}
 @PostMapping("/otp/request") public OtpResponse request(@Valid @RequestBody OtpRequest r){return service.request(r);}
 @PostMapping("/otp/verify") public AuthResponse verify(@Valid @RequestBody OtpVerifyRequest r){return service.verify(r);}
 @GetMapping("/me") public UserResponse me(Principal p){return service.current(p.getName());}
 @PutMapping("/me") public UserResponse update(Principal p,@Valid @RequestBody UpdateProfileRequest r){return service.update(p.getName(),r);}
}
