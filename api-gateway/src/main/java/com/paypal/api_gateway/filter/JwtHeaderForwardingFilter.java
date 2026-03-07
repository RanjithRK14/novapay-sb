package com.paypal.api_gateway.filter;

import com.paypal.api_gateway.model.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtHeaderForwardingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        try {
            String token = authHeader.substring(7);
            Claims claims = JwtUtil.validateToken(token);

            Object userIdObj = claims.get("userId");
            if (userIdObj == null) return chain.filter(exchange);

            String userId = String.valueOf(userIdObj);
            String role   = claims.get("role") != null ? String.valueOf(claims.get("role")) : "";
            String email  = claims.getSubject();

            ServerWebExchange mutated = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header("X-User-Id",    userId)
                            .header("X-User-Email", email)
                            .header("X-User-Role",  role)
                            .build())
                    .build();

            return chain.filter(mutated);

        } catch (Exception e) {
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() { return -1; }
}