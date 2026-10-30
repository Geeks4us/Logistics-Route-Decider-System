package com.logistics.optimization.application;

import com.logistics.optimization.domain.RouteOptimizationRequest;
import com.logistics.optimization.domain.RouteOptimizationResult;
import com.logistics.optimization.ports.RoutingEnginePort;

/** Framework-neutral application service; provider access is hidden behind RoutingEnginePort. */
public class RouteOptimizationService implements OptimizeRouteUseCase {
    private final RoutingEnginePort routingEngine;

    public RouteOptimizationService(RoutingEnginePort routingEngine) {
        this.routingEngine = routingEngine;
    }

    @Override
    public RouteOptimizationResult optimize(RouteOptimizationRequest request) {
        return routingEngine.calculate(request);
    }
}
