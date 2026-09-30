package com.example.moviebooking.notification;
import org.springframework.stereotype.Service;
@Service public class NotificationService {
 private final NotificationRepository notifications;
 public NotificationService(NotificationRepository notifications){this.notifications=notifications;}
 public String record(String userId,String type,String message){var n=new Notification();n.userId=userId;n.type=type;n.message=message;notifications.save(n);return message;}
}
