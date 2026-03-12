package com.kelab.cloud.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("===== JWT FILTER INICIADO =====");
        System.out.println("Request URI: " + request.getRequestURI());

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null) {
            System.out.println("❌ No Authorization header");
            filterChain.doFilter(request, response);
            return;
        }

        if (!authHeader.startsWith("Bearer ")) {
            System.out.println("❌ Authorization no empieza con Bearer");
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("✅ Authorization header recibido");

        final String token = authHeader.substring(7);
        System.out.println("Token extraído: " + token);

        try {

            final String username = jwtService.extractUsername(token);
            System.out.println("Username extraído del token: " + username);

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                System.out.println("Usuario cargado desde BD: " + userDetails.getUsername());
                System.out.println("Authorities del usuario: " + userDetails.getAuthorities());

                boolean isValid = jwtService.validateToken(token, username);
                System.out.println("¿Token válido?: " + isValid);

                if (isValid) {

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authToken);

                    System.out.println("✅ Usuario autenticado correctamente en contexto");
                } else {
                    System.out.println("❌ Token inválido");
                }
            }

        } catch (JwtException e) {
            System.out.println("❌ JWT Exception: " + e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (IllegalArgumentException e) {
            System.out.println("❌ IllegalArgumentException: " + e.getMessage());
            SecurityContextHolder.clearContext();
        }

        System.out.println("===== JWT FILTER FINALIZADO =====");

        filterChain.doFilter(request, response);
    }
}