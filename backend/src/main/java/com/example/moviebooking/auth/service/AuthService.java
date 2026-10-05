package com.example.moviebooking.auth.service;
import com.example.moviebooking.auth.dto.*;
import com.example.moviebooking.auth.entity.*;
import com.example.moviebooking.auth.repository.UserRepository;
import com.example.moviebooking.auth.security.JwtService;
import com.example.moviebooking.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import java.util.Locale;
@Service public class AuthService {
 private final UserRepository users; private final OtpService otp; private final MobileNormalizer mobile; private final JwtService jwt;
 public AuthService(UserRepository users,OtpService otp,MobileNormalizer mobile,JwtService jwt){this.users=users;this.otp=otp;this.mobile=mobile;this.jwt=jwt;}
 public OtpResponse request(OtpRequest r){return otp.request(mobile.normalize(r.mobile()));}
 public OtpResponse requestEmail(EmailOtpRequest r){String address=r.email().trim().toLowerCase(Locale.ROOT);if(users.findByEmailIgnoreCase(address).filter(u->u.enabled).isEmpty())throw new ApiException(404,"EMAIL_NOT_REGISTERED","No enabled account uses this email address");return otp.requestEmail(address);}
 @Transactional(noRollbackFor=ApiException.class) public AuthResponse verifyEmail(EmailOtpVerifyRequest r){String address=r.email().trim().toLowerCase(Locale.ROOT);otp.verifyEmail(address,r.otp());var user=users.findByEmailIgnoreCase(address).filter(u->u.enabled).orElseThrow(()->new ApiException(401,"UNAUTHORIZED","Unable to authenticate this account"));return new AuthResponse(UserResponse.of(user),jwt.generate(user),"Bearer");}
 @Transactional(noRollbackFor=ApiException.class) public AuthResponse verify(OtpVerifyRequest r){
 String number=mobile.normalize(r.mobile());otp.verify(number,r.otp());
 var user=users.findByMobile(number).orElseGet(()->{var u=new User();u.mobile=number;return users.save(u);});
 if(!user.enabled)throw new ApiException(401,"UNAUTHORIZED","Unable to authenticate this account");
 return new AuthResponse(UserResponse.of(user),jwt.generate(user),"Bearer");}
 public User get(String id){return users.findById(id).orElseThrow(()->new ApiException(404,"USER_NOT_FOUND","User not found"));}
 public UserResponse current(String id){return UserResponse.of(get(id));}
 @Transactional public UserResponse update(String id,UpdateProfileRequest r){var u=get(id);String email=r.email()==null||r.email().isBlank()?null:r.email().trim().toLowerCase(Locale.ROOT);if(email!=null)users.findByEmailIgnoreCase(email).filter(other->!other.id.equals(id)).ifPresent(other->{throw new ApiException(409,"DUPLICATE_EMAIL","Email is already in use");});u.name=r.name().trim();u.email=email;return UserResponse.of(users.saveAndFlush(u));}
 @Transactional public UserResponse register(String id,RegistrationRequest r){var u=get(id);String email=r.email()==null||r.email().isBlank()?null:r.email().trim().toLowerCase(Locale.ROOT);if(email!=null)users.findByEmailIgnoreCase(email).filter(other->!other.id.equals(id)).ifPresent(other->{throw new ApiException(409,"DUPLICATE_EMAIL","Email is already in use");});u.name=r.name().trim();u.email=email;return UserResponse.of(users.saveAndFlush(u));}
 public Page<UserResponse> list(int page,int size){return users.findAll(PageRequest.of(Math.max(0,page),Math.max(1,Math.min(size,100)),Sort.by("createdAt").descending())).map(UserResponse::of);}
 @Transactional public UserResponse role(String id,Role role){var u=get(id);u.role=role;return UserResponse.of(users.save(u));}
 @Transactional public UserResponse status(String id,boolean enabled){var u=get(id);u.enabled=enabled;return UserResponse.of(users.save(u));}
}
