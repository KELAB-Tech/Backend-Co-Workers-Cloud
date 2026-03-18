package com.kelab.cloud.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.kelab.cloud.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                        response.setContentType("application/json");
                                                        response.getWriter().write("{\"error\": \"Unauthorized\"}");
                                                })
                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                                        response.setContentType("application/json");
                                                        response.getWriter().write("{\"error\": \"Forbidden\"}");
                                                }))

                                .authorizeHttpRequests(auth -> auth

                                                // ── AUTH ────────────────────────────────────────
                                                .requestMatchers("/api/auth/**").permitAll()

                                                // ── PREFLIGHT ───────────────────────────────────
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                                // ── MARKETPLACE PÚBLICO (GET) ───────────────────

                                                // Productos: marketplace global, detalle, imágenes, categoría
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/products/marketplace",
                                                                "/api/products/marketplace/**",
                                                                "/api/products/category/**",
                                                                "/api/products/{id}",
                                                                "/api/products/{id}/images",
                                                                "/api/products/store/**")
                                                .permitAll()

                                                // Categorías: listar activas (público)
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/categories")
                                                .permitAll()

                                                // Tiendas: listar aprobadas, detalle, imágenes, productos
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/store",
                                                                "/api/store/{id}",
                                                                "/api/store/{id}/images",
                                                                "/api/store/store/{storeId}")
                                                .permitAll()

                                                // ── TODO LO DEMÁS REQUIERE AUTH ─────────────────
                                                .anyRequest().authenticated())

                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12);
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {

                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(List.of(
                                "http://localhost:3000",
                                "http://localhost:3001", "http://localhost:3002", "https://cloud.kelab.com.co/",
                                "https://admin.cloud.kelab.com.co/", "https://app.cloud.kelab.com.co/"));

                configuration.setAllowedMethods(List.of(
                                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

                configuration.setAllowedHeaders(List.of("*"));

                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}