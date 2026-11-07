package com.parkgrid.web;
import com.parkgrid.service.AuthService;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final AuthService auth;
 public AuthController(AuthService auth){this.auth=auth;}
 public record Register(@Email @NotBlank String email,@Size(min=8) String password){}
 public record Login(@Email @NotBlank String email,@NotBlank String password){}
 @PostMapping("/register") public Map<String,Object> register(@Valid @RequestBody Register r){return auth.register(r.email(),r.password());}
 @PostMapping("/login") public Map<String,Object> login(@Valid @RequestBody Login r){return auth.login(r.email(),r.password());}
}
