package com.visulaw.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(
                        "auth-provider",
                        r -> r.path("/auth/**")
                                .uri("http://auth-provider:8081")
                )
                .route(
                        "legal-service",
                        r -> r.path("/legal/**")
                                .uri("http://legal-service:8082")
                )
                .build();
    }

}
