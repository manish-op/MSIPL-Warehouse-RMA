package com.serverManagement.server.management.config;

import com.serverManagement.server.management.filter.JWTRequestFilter;
import com.serverManagement.server.management.service.admin.login.AdminUserLogin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // <-- IMPORT THIS
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JWTRequestFilter JWTFilter;
    private final AdminUserLogin adminUserLogin;

    public SecurityConfig(JWTRequestFilter jWTFilter, AdminUserLogin adminUserLogin) {
        super();
        JWTFilter = jWTFilter;
        this.adminUserLogin = adminUserLogin;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminUserLogin);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // --- FIX 1: ADD THIS LINE TO ALLOW PREFLIGHT REQUESTS ---
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        .requestMatchers(
                                // Corrected all paths to include /api prefix
                                "/api/admin/user/password",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api/keyword/getSubKeyword",
                                "/api/admin/user/test",
                                "/api/admin/user/login",
                                "/api/region/getList",
                                "/api/keyword/keywordList",
                                "/api/thresholds/**",
                                "/api/alerts/**",
                                "/ws/**",
                                "/api/items/**") // Fixed typo api/items -> /api/items
                        .permitAll()
                        // Explicitly secure all RMA endpoints
                        .requestMatchers("/api/rma/**").authenticated()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint((request, response, e) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json");
                    response.getWriter().write("You are not authenticated.");
                }));

        http.addFilterBefore(JWTFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();

        // --- FIX 2: PUT ALL ORIGINS IN *ONE* LIST ---
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000", // for development
                "http://mswarehouse.store", // for production
                "https://mswarehouse.store" // for production
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Content-Disposition"));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply to all paths
        return source;
    }
}