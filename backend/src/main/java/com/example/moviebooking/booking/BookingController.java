package com.example.moviebooking.booking;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.*;
@RestController @RequestMapping("/api/v1/bookings")
public class BookingController {
 private final BookingService service;
 public BookingController(BookingService service){this.service=service;}
 @GetMapping("/me") public List<Booking> mine(Principal p){return service.mine(p);}
 @GetMapping("/{id}") public Booking get(@PathVariable String id,Principal p){return service.get(id,p);}
 @PostMapping public Booking create(Principal p,@Valid @RequestBody CreateBookingRequest r){return service.create(p,r);}
 @PostMapping("/{id}/cancel") public Booking cancel(@PathVariable String id,Principal p){return service.cancel(id,p);}
 @PostMapping("/admin/{id}/confirm") public Booking confirm(@PathVariable String id){return service.adminConfirm(id);}
 @PostMapping("/admin/{id}/attendance") public Booking attended(@PathVariable String id,@RequestParam boolean attended){return service.markAttended(id,attended);}
}
