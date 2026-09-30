package com.example.moviebooking;
import com.example.moviebooking.auth.entity.*;
import com.example.moviebooking.auth.repository.*;
import com.example.moviebooking.auth.provider.SmsProvider;
import com.example.moviebooking.auth.security.JwtService;
import com.example.moviebooking.auth.service.MobileNormalizer;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles({"test","dev"})
class AuthIntegrationTest {
 @Autowired MockMvc mvc; @Autowired ObjectMapper json; @Autowired UserRepository users;
 @Autowired OtpVerificationRepository otps; @Autowired JwtService jwt; @Autowired MobileNormalizer normalizer;
 @MockitoBean SmsProvider sms;
 final Map<String,String> codes=new ConcurrentHashMap<>();
 @BeforeEach void setup(){otps.deleteAll();users.deleteAll();codes.clear();doAnswer(i->{codes.put(i.getArgument(0),i.getArgument(1));return null;}).when(sms).sendOtp(anyString(),anyString());}
 String body(Object value)throws Exception{return json.writeValueAsString(value);}
 void request(String mobile,int status)throws Exception{mvc.perform(post("/api/v1/auth/otp/request").contentType("application/json").content(body(Map.of("mobile",mobile)))).andExpect(status().is(status));}
 JsonNode verify(String mobile,String code,int status)throws Exception{var result=mvc.perform(post("/api/v1/auth/otp/verify").contentType("application/json").content(body(Map.of("mobile",mobile,"otp",code)))).andExpect(status().is(status)).andReturn();return json.readTree(result.getResponse().getContentAsString());}
 JsonNode login(String mobile)throws Exception{request(mobile,200);return verify(mobile,codes.get(normalizer.normalize(mobile)),200);}
 String token(JsonNode auth){return "Bearer "+auth.get("accessToken").asText();}
 @Test void fullAccountFlowAndProfile()throws Exception{
 var auth=login("9876543210");assertEquals("USER",auth.at("/user/role").asText());assertTrue(auth.at("/user/email").isNull());assertEquals("+919876543210",auth.at("/user/mobile").asText());UUID.fromString(auth.at("/user/id").asText());
 var claims=jwt.validate(auth.get("accessToken").asText());assertEquals(auth.at("/user/id").asText(),claims.getSubject());assertEquals("+919876543210",claims.getClaimAsString("mobile"));assertEquals("USER",claims.getClaimAsString("role"));
 mvc.perform(get("/api/v1/auth/me").header("Authorization",token(auth))).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Movie lover"));
 mvc.perform(put("/api/v1/auth/me").header("Authorization",token(auth)).contentType("application/json").content(body(Map.of("name","John","email","JOHN@example.com")))).andExpect(status().isOk()).andExpect(jsonPath("$.email").value("john@example.com"));
 mvc.perform(put("/api/v1/auth/me").header("Authorization",token(auth)).contentType("application/json").content("{\"name\":\"John\",\"email\":null}")).andExpect(status().isOk()).andExpect(jsonPath("$.email").doesNotExist());
 verify("9876543210",codes.get("+919876543210"),400);
 var record=otps.findFirstByMobileOrderByIdDesc("+919876543210").orElseThrow();record.createdAt=Instant.now().minusSeconds(31);otps.save(record);
 var again=login("9876543210");assertEquals(auth.at("/user/id"),again.at("/user/id"));assertEquals(1,users.count());
 }
 @Test void otpHashDeliveryExpiryAndLimits()throws Exception{
 request("9876543210",200);String code=codes.get("+919876543210");assertTrue(code.matches("[0-9]{6}"));var record=otps.findFirstByMobileOrderByIdDesc("+919876543210").orElseThrow();assertNotEquals(code,record.otpHash);assertTrue(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().matches(code,record.otpHash));request("+91 9876543210",429);
 String wrong=code.equals("000000")?"000001":"000000";for(int i=0;i<5;i++)verify("9876543210",wrong,400);verify("9876543210",code,429);assertEquals(5,otps.findById(record.id).orElseThrow().attemptCount);
 record=otps.findById(record.id).orElseThrow();record.createdAt=Instant.now().minusSeconds(31);otps.save(record);request("9876543210",200);
 record=otps.findFirstByMobileOrderByIdDesc("+919876543210").orElseThrow();record.expiresAt=Instant.now().minusSeconds(1);otps.save(record);verify("9876543210",codes.get("+919876543210"),400);
 }
 @Test void hourlyLimitAndOldCodeInvalidation()throws Exception{
 request("9876543210",200);String old=codes.get("+919876543210");
 for(int i=1;i<10;i++){var record=otps.findFirstByMobileOrderByIdDesc("+919876543210").orElseThrow();record.createdAt=Instant.now().minusSeconds(31);otps.save(record);request("9876543210",200);}
 var latest=otps.findFirstByMobileOrderByIdDesc("+919876543210").orElseThrow();latest.createdAt=Instant.now().minusSeconds(31);otps.save(latest);request("9876543210",429);
 if(!old.equals(codes.get("+919876543210")))verify("9876543210",old,400);
 }
 @Test void authorizationUsesCurrentDatabaseRoleAndStatus()throws Exception{
 mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());mvc.perform(get("/api/v1/auth/me").header("Authorization","Bearer invalid")).andExpect(status().isUnauthorized());
 var auth=login("9876543210");mvc.perform(get("/api/v1/admin/users").header("Authorization",token(auth))).andExpect(status().isForbidden());
 var user=users.findByMobile("+919876543210").orElseThrow();user.role=Role.ADMIN;users.save(user);
 mvc.perform(get("/api/v1/admin/users").header("Authorization",token(auth))).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].mobile").value(user.mobile));
 var other=login("9876543211");String id=other.at("/user/id").asText();
 mvc.perform(get("/api/v1/admin/users/"+id).header("Authorization",token(auth))).andExpect(status().isOk());
 mvc.perform(patch("/api/v1/admin/users/"+id+"/role").header("Authorization",token(auth)).contentType("application/json").content("{\"role\":\"ADMIN\"}")).andExpect(status().isOk());
 mvc.perform(get("/api/v1/admin/users").header("Authorization",token(other))).andExpect(status().isOk());
 mvc.perform(patch("/api/v1/admin/users/"+id+"/status").header("Authorization",token(auth)).contentType("application/json").content("{\"enabled\":false}")).andExpect(status().isOk());
 mvc.perform(get("/api/v1/auth/me").header("Authorization",token(other))).andExpect(status().isUnauthorized());
 var record=otps.findFirstByMobileOrderByIdDesc("+919876543211").orElseThrow();record.createdAt=Instant.now().minusSeconds(31);otps.save(record);request("9876543211",200);verify("9876543211",codes.get("+919876543211"),401);
 }
 @Test void validationAndUniqueEmail()throws Exception{
 request("bad",400);mvc.perform(post("/api/v1/auth/otp/request").contentType("application/json").content("{invalid")).andExpect(status().isBadRequest());
 var first=login("9876543210");var second=login("9876543211");String profile="{\"name\":\"John\",\"email\":\"john@example.com\"}";
 mvc.perform(put("/api/v1/auth/me").header("Authorization",token(first)).contentType("application/json").content(profile)).andExpect(status().isOk());
 mvc.perform(put("/api/v1/auth/me").header("Authorization",token(second)).contentType("application/json").content(profile)).andExpect(status().isConflict());
 for(String field:List.of("role","mobile","enabled","id","createdAt"))mvc.perform(put("/api/v1/auth/me").header("Authorization",token(first)).contentType("application/json").content(body(Map.of("name","John",field,"ADMIN")))).andExpect(status().isBadRequest());
 mvc.perform(put("/api/v1/auth/me").header("Authorization",token(first)).contentType("application/json").content(body(Map.of("name"," ","email","bad")))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.name").exists());
 }
 @Test void corsAndExpiredToken()throws Exception{
 mvc.perform(options("/api/v1/auth/me").header("Origin","http://localhost:5173").header("Access-Control-Request-Method","GET").header("Access-Control-Request-Headers","authorization")).andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:5173"));
 var auth=login("9876543210");var user=users.findById(auth.at("/user/id").asText()).orElseThrow();var shortJwt=new JwtService("test-only-secret-with-at-least-thirty-two-bytes",1);String expired=shortJwt.generate(user);Thread.sleep(20);
 mvc.perform(get("/api/v1/auth/me").header("Authorization","Bearer "+expired)).andExpect(status().isUnauthorized());
 }
 @Test void concurrentVerificationConsumesCodeOnce()throws Exception{
 request("9876543210",200);String content=body(Map.of("mobile","9876543210","otp",codes.get("+919876543210")));
 try(var pool=Executors.newFixedThreadPool(2)){Callable<Integer> call=()->mvc.perform(post("/api/v1/auth/otp/verify").contentType("application/json").content(content)).andReturn().getResponse().getStatus();var results=pool.invokeAll(List.of(call,call));var statuses=new ArrayList<Integer>();for(var result:results)statuses.add(result.get());Collections.sort(statuses);assertEquals(List.of(200,400),statuses);assertEquals(1,users.count());}
 }
 @Test void smsFailureRollsBackChallenge()throws Exception{doThrow(new com.example.moviebooking.common.exception.ApiException(503,"SMS_UNAVAILABLE","Unavailable")).when(sms).sendOtp(anyString(),anyString());request("9876543210",503);assertEquals(0,otps.count());}
}
