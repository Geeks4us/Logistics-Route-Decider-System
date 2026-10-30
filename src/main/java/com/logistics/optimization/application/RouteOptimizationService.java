package com.logistics.optimization.application;

import com.logistics.optimization.domain.RouteOptimizationRequest;
import com.logistics.optimization.domain.RouteOptimizationResult;
import com.logistics.optimization.ports.RoutingEnginePort;
import org.springframework.stereotype.Service;

/** Application service depends on an abstraction, never on a map-provider SDK. */
@Service
public class RouteOptimizationService implements OptimizeRouteUseCase {
    private final RoutingEnginePort routingEngine;

    public RouteOptimizationService(RoutingEnginePort routingEngine) {
        this.routingEngine = routingEngine;
    }

    @Override
    public RouteOptimizationResult optimize(RouteOptimizationRequest request) {
        // Validate domain constraints before delegating to the routing port.
        return routingEngine.calculate(request);
    }
}
