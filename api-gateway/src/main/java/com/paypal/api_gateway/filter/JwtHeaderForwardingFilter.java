package com.paypal.api_gateway.filter;

import com.paypal.api_gateway.model.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class JwtHeaderForwardingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path   = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        // 1. Always pass OPTIONS (CORS preflight) through
        if (HttpMethod.OPTIONS.equals(method)) {
            return chain.filter(exchange);
        }

        // 2. Public auth endpoints — no token required
        if (path.startsWith("/auth/") || path.equals("/auth")) {
            return chain.filter(exchange);
        }

        // 3. Health-check / keep-alive pings — no token required
        //    Covers /ping/user, /ping/wallet, /ping/transaction, /ping/reward, /ping/notification
        if (path.startsWith("/ping") || path.equals("/ping")) {
            return chain.filter(exchange);
        }

        // 4. All other routes require a valid Bearer token
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.err.println("[JwtFilter] Missing Authorization header: " + path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token;
        Map<String, Object> claims;
        try {
            token  = authHeader.substring(7).trim();
            claims = JwtUtil.validateToken(token);
        } catch (Exception e) {
            System.err.println("[JwtFilter] JWT invalid for " + path
                    + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String username  = (String) claims.get("sub");
        String role      = (String) claims.get("role");
        Object userIdObj = claims.get("userId");

        if (username == null || username.isBlank()) {
            System.err.println("[JwtFilter] JWT missing 'sub' for " + path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        final String userId    = userIdObj != null ? String.valueOf(userIdObj) : "";
        final String finalRole = (role != null && !role.isBlank()) ? role : "ROLE_USER";
        final String finalUser = username;

        HttpHeaders newHeaders = new HttpHeaders();
        newHeaders.addAll(exchange.getRequest().getHeaders());
        newHeaders.set("X-User-Id",    userId);
        newHeaders.set("X-User-Email", finalUser);
        newHeaders.set("X-User-Role",  finalRole);

        ServerHttpRequest decoratedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                return newHeaders;
            }
        };

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(decoratedRequest)
                .build();

        System.out.println("[JwtFilter] OK user=" + finalUser
                + " userId=" + userId + " → " + path);
        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() { return -1; }
}
