package com.visulaw.api_gateway.filters;

import com.visulaw.api_gateway.util.JwtUtil;
import com.visulaw.api_gateway.validator.RouteValidator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@RefreshScope
@RequiredArgsConstructor
public class AuthFilter implements GatewayFilter {

    private static final String HEADER_AUTHORIZATION = "Authorization";

    @Autowired
    private final RouteValidator routeValidator;

    @Autowired
    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = "";
        ServerHttpRequest request = exchange.getRequest();

        if (routeValidator.isSecured.test(request)) {
            System.out.println("Secured endpoint is being hit! Authenticating...");

            if (isCredentialsMissing(request)) {
                System.out.println("Credentials missing");
                return onError(exchange);
            }

            token = request.getHeaders().get(HEADER_AUTHORIZATION).toString().split(" ")[1];

            if (jwtUtil.isInvalid(token)) {
                System.out.println("Invalid token");
                return onError(exchange);
            } else {
                System.out.println("Authentication successful!");
            }

            populateRequestWithHeaders(exchange, token);
        }

        return chain.filter(exchange);
    }

    private void populateRequestWithHeaders(ServerWebExchange exchange, String token) {
        Claims claims = jwtUtil.getAllClaims(token);
        exchange.getRequest()
                .mutate()
                .header("id", String.valueOf(claims.get("id")))
                .header("role", String.valueOf(claims.get("role")))
                .build();
    }

    private Mono<Void> onError(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "Unauthorized";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        response.writeWith(Mono.just(buffer));

        return response.setComplete();
    }

    private boolean isCredentialsMissing(ServerHttpRequest request) {
        return !(request.getHeaders().containsKey(HEADER_AUTHORIZATION));
    }
}
