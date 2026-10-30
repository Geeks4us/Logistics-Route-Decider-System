package com.logistics.dispatch.application;

import com.logistics.dispatch.domain.Route;
import com.logistics.dispatch.ports.RouteRepository;
import com.logistics.optimization.application.OptimizeRouteUseCase;
import java.util.UUID;

/** Dispatch depends on the Optimization capability's public contract, not its implementation. */
public class DispatchService {
    private final RouteRepository routes;
    private final OptimizeRouteUseCase optimizer;

    public DispatchService(RouteRepository routes, OptimizeRouteUseCase optimizer) {
        this.routes = routes;
        this.optimizer = optimizer;
    }

    public Route findRoute(UUID routeId) {
        return routes.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + routeId));
    }

    // TODO: Add the use case that validates a batch, invokes optimizer.optimize(...),
    // persists the resulting route, and emits a local RouteOptimized event.
}
