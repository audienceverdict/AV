package com.example.moviebooking.auth.service;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import com.example.moviebooking.auth.entity.*;
import com.example.moviebooking.auth.repository.UserRepository;
@Component @Profile("dev") public class DevelopmentAdminSeed implements ApplicationRunner {
 private final UserRepository users; private final MobileNormalizer normalizer; private final String mobile;
 public DevelopmentAdminSeed(UserRepository users,MobileNormalizer normalizer,@Value("${app.admin.mobile:}") String mobile){this.users=users;this.normalizer=normalizer;this.mobile=mobile;}
 @Transactional public void run(ApplicationArguments args){if(mobile.isBlank())return;String number=normalizer.normalize(mobile);var u=users.findByMobile(number).orElseGet(()->{var fresh=new User();fresh.mobile=number;fresh.name="Administrator";return fresh;});u.role=Role.ADMIN;users.save(u);}
}
