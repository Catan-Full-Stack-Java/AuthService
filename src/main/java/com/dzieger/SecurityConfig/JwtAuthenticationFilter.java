package com.dzieger.SecurityConfig;

import com.dzieger.services.PlayerService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final PlayerService playerService;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, PlayerService playerService) {
        this.jwtUtil = jwtUtil;
        this.playerService = playerService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String username = playerService.getUsernameByUserId(UUID.fromString(token));
                setAuthenticationContext(username, token, request);
            } catch (Exception e) {
                handleTokenException(e);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void setAuthenticationContext(String username, String token, HttpServletRequest request) {
        UserDetails userDetails = playerService.loadUserByUsername(username);

        if (jwtUtil.validate(token)) {
            List<SimpleGrantedAuthority> authorities = jwtUtil.extractAuthorities(token).stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
            var authToken = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }

    private void handleTokenException(Exception e) {
        logger.error("Cannot set user authentication: {}", e);
    }

}
