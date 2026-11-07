package com.parkgrid.security;

import io.jsonwebtoken.JwtException;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.*; import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Configuration @EnableMethodSecurity
public class SecurityConfig {
    @Bean SecurityFilterChain filterChain(HttpSecurity http,JwtService jwt) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).cors(c->c.configurationSource(corsConfigurationSource()))
            .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a->a.requestMatchers(org.springframework.http.HttpMethod.OPTIONS,"/**")
                .permitAll().requestMatchers("/internal/billing/**")
                .permitAll().requestMatchers(org.springframework.http.HttpMethod.POST,"/api/auth/login","/api/auth/register")
                .permitAll().requestMatchers("/actuator/health", "/error").permitAll().anyRequest().authenticated())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((r, p, ex) -> {
                    p.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    p.setContentType("application/json");
                    p.getWriter().write("{\"error\":\"Authentication required\"}");
                })
                .accessDeniedHandler((r, p, ex) -> {
                    p.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    p.setContentType("application/json");
                    p.getWriter().write("{\"error\":\"Access denied\"}");
                }))
            .addFilterBefore(new OncePerRequestFilter(){protected void doFilterInternal(HttpServletRequest r,HttpServletResponse p,FilterChain c)throws ServletException,IOException{
                String h=r.getHeader("Authorization");
                if(h!=null&&h.startsWith("Bearer ")){try{var claims=jwt.parse(h.substring(7)).getPayload();var auth=new UsernamePasswordAuthenticationToken(claims.getSubject(),null,java.util.List.of(new SimpleGrantedAuthority("ROLE_"+claims.get("role",String.class))));org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);}catch(JwtException ignored){}}
                c.doFilter(r,p);}},UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setExposedHeaders(List.of("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
