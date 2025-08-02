package com.visulaw.api_gateway.validator;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> unprotectedEndpoints = List.of("/login");

    public Predicate<ServerHttpRequest> isSecured = req -> unprotectedEndpoints.stream()
            .noneMatch(uri -> req.getURI().getPath().contains(uri));

}
