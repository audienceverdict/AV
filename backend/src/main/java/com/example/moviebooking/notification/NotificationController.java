package com.example.moviebooking.notification;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.*;
@RestController @RequestMapping("/api/v1/notifications")
public class NotificationController { private final NotificationRepository notifications; public NotificationController(NotificationRepository notifications){this.notifications=notifications;} @GetMapping public List<Notification> mine(Principal p){return notifications.findByUserIdOrderByCreatedAtDesc(p.getName());} }
