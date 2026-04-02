package com.example.demo.config;

import com.example.demo.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtChannelInterceptorTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final JwtChannelInterceptor interceptor = new JwtChannelInterceptor(jwtService);
    private final MessageChannel channel = mock(MessageChannel.class);

    @Test
    void preSend_returnsMessageOnNullAccessor() {
        // Message without accessor
        Message<String> message = MessageBuilder.withPayload("test").build();
        Message<?> result = interceptor.preSend(message, channel);
        assertEquals(message, result);
    }

    @Test
    void preSend_returnsMessageOnNonConnectCommand() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        Message<String> message = MessageBuilder.createMessage("test", accessor.getMessageHeaders());
        
        Message<?> result = interceptor.preSend(message, channel);
        
        assertEquals(message, result);
        assertNull(accessor.getUser());
    }

    @Test
    void preSend_withValidToken_setsUser() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer valid-token");
        accessor.setLeaveMutable(true);
        Message<String> message = MessageBuilder.createMessage("test", accessor.getMessageHeaders());

        when(jwtService.isValid("valid-token")).thenReturn(true);
        when(jwtService.getEmail("valid-token")).thenReturn("user@test.com");
        when(jwtService.getUserId("valid-token")).thenReturn(1L);

        Message<?> result = interceptor.preSend(message, channel);

        assertNotNull(result);
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        Authentication auth = (Authentication) resultAccessor.getUser();
        assertNotNull(auth);
        assertEquals("user@test.com", auth.getName());
    }

    @Test
    void preSend_withInvalidToken_doesNotSetUser() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer invalid-token");
        Message<String> message = MessageBuilder.createMessage("test", accessor.getMessageHeaders());

        when(jwtService.isValid("invalid-token")).thenReturn(false);

        interceptor.preSend(message, channel);

        assertNull(accessor.getUser());
    }

    @Test
    void preSend_withoutBearer_doesNotSetUser() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "invalid-token");
        Message<String> message = MessageBuilder.createMessage("test", accessor.getMessageHeaders());

        interceptor.preSend(message, channel);

        assertNull(accessor.getUser());
    }

    @Test
    void preSend_withNullAuthHeader_doesNotSetUser() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        Message<String> message = MessageBuilder.createMessage("test", accessor.getMessageHeaders());

        interceptor.preSend(message, channel);

        assertNull(accessor.getUser());
    }
}
