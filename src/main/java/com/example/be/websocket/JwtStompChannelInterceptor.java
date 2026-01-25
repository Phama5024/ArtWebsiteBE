package com.example.be.websocket;

import com.example.be.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtStompChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String auth = firstHeader(accessor, "Authorization");
            if (auth == null) auth = firstHeader(accessor, "authorization");

            if (auth == null || !auth.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Missing Authorization Bearer token");
            }

            String token = auth.substring(7);

            if (!jwtService.isTokenValid(token)) {
                throw new IllegalArgumentException("Invalid token");
            }

            String email = jwtService.extractEmail(token);
            String role = jwtService.extractRole(token);

            if (email == null || role == null) {
                throw new IllegalArgumentException("Invalid token claims");
            }

            UsernamePasswordAuthenticationToken user =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            accessor.setUser(user);
        }

        return message;
    }

    private String firstHeader(StompHeaderAccessor accessor, String key) {
        List<String> values = accessor.getNativeHeader(key);
        return (values == null || values.isEmpty()) ? null : values.get(0);
    }
}
