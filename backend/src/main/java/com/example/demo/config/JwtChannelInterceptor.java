package com.example.demo.config;

import com.example.demo.security.JwtService;
import lombok.RequiredArgsConstructor; 
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import org.springframework.lang.Nullable;

@RequiredArgsConstructor
@Slf4j
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    @Nullable
    public Message<?> preSend(@Nullable Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // Only intercept CONNECT commands (when client first connects)
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (jwtService.isValid(token)) {
                    String email = jwtService.getEmail(token);
                    Long userId = jwtService.getUserId(token);

                    // Create authentication token
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            email,           // principal (email)
                            userId,          // credentials (userId — useful later)
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );

                    // Set the authenticated user on the WebSocket session
                    accessor.setUser(auth);

                    log.info("WebSocket: User {} (ID: {}) connected", email, userId);
                } else {
                    log.warn("WebSocket: Invalid JWT token received");
                    // Don't throw exception — let unauthenticated connection
                    // be handled by subscription-level security if needed
                }
            } else {
                log.debug("WebSocket: No Authorization header in CONNECT frame");
            }
        }

        return message;
    }
}