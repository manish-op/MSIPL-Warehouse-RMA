package com.serverManagement.server.management.filter;

import com.serverManagement.server.management.service.admin.login.AdminUserLogin;
import com.serverManagement.server.management.utils.jwt.JWTUtils;
import io.jsonwebtoken.ExpiredJwtException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// ----------------------------------------

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    // --- Add standard logger ---
    private static final Logger log = LoggerFactory.getLogger(JwtChannelInterceptor.class);
    // --------------------------------

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AdminUserLogin loginService; // Your UserDetailsService

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            log.error("StompHeaderAccessor is null");
            throw new MessagingException("Internal server error (null accessor)");
        }

        log.info("STOMP Command: {}", accessor.getCommand());

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.info("Authorization Header: {}", authHeader);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("Missing or invalid Authorization header");
                throw new MessagingException("Missing or invalid Authorization header");
            }

            String jwt = authHeader.substring(7);

            try {
                String username = jwtUtils.getUsernameFromToken(jwt);
                if (username != null) {
                    UserDetails userDetails = this.loginService.loadUserByUsername(username);

                    if (jwtUtils.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                        // This is the most important line:
                        accessor.setUser(authentication);
                        log.info("Authenticated user: {}", username);
                    } else {
                        log.warn("Invalid JWT token for user: {}", username);
                        throw new MessagingException("Invalid JWT token");
                    }
                } else {
                    log.warn("Username from token is null");
                    throw new MessagingException("Invalid JWT token: Username not found");
                }
            } catch (ExpiredJwtException e) {
                log.error("JWT Token is expired: {}", e.getMessage());
                throw new MessagingException("JWT Token is expired");
            } catch (Exception e) {
                log.error("Error authenticating JWT token: {}", e.getMessage(), e);
                throw new MessagingException("Authentication failed: " + e.getMessage());
            }
        }
        return message;
    }
}