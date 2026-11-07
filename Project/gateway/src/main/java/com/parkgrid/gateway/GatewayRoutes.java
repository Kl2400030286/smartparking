package com.parkgrid.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutes {
    @Bean
    RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("booking-api", r -> r.path("/api/bookings/**")
                        .uri("lb://BOOKING-SERVICE"))
                .route("billing-api", r -> r.path("/api/billing/**")
                        .uri("lb://BILLING-SERVICE"))
                .route("parking-auth-api", r -> r.path("/api/auth/**")
                        .uri("lb://PARKING-SERVICE"))
                .route("parking-api", r -> r.path("/api/parking/**")
                        .uri("lb://PARKING-SERVICE"))
                .route("parking-admin-api", r -> r.path("/api/admin/**")
                        .uri("lb://PARKING-SERVICE"))
                .route("parking-health-api", r -> r.path("/actuator/health")
                        .uri("lb://PARKING-SERVICE"))
                .build();
    }
}
