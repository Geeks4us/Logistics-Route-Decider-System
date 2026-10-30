package com.logistics.config;

import com.logistics.dispatch.application.DispatchService;
import com.logistics.dispatch.ports.RouteRepository;
import com.logistics.optimization.application.OptimizeRouteUseCase;
import com.logistics.optimization.application.RouteOptimizationService;
import com.logistics.optimization.ports.RoutingEnginePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring composition root. Concrete persistence and mapping adapters must be
 * supplied by the infrastructure layer; application services stay framework-neutral.
 */
@Configuration
public class ApplicationConfiguration {

    @Bean
    public OptimizeRouteUseCase optimizeRouteUseCase(RoutingEnginePort routingEnginePort) {
        return new RouteOptimizationService(routingEnginePort);
    }

    @Bean
    public DispatchService dispatchService(
            RouteRepository routeRepository,
            OptimizeRouteUseCase optimizeRouteUseCase) {
        return new DispatchService(routeRepository, optimizeRouteUseCase);
    }
}
