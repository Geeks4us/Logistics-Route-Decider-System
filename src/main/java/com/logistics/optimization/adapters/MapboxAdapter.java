package com.logistics.optimization.adapters;

import com.logistics.optimization.domain.RouteOptimizationRequest;
import com.logistics.optimization.domain.RouteOptimizationResult;
import com.logistics.optimization.ports.RoutingEnginePort;
import org.springframework.stereotype.Component;

/** Infrastructure adapter; replace this skeleton with the Mapbox client integration. */
@Component
public class MapboxAdapter implements RoutingEnginePort {
    @Override
    public RouteOptimizationResult calculate(RouteOptimizationRequest request) {
        // TODO: Call Mapbox, translate provider DTOs, and handle provider errors here.
        throw new UnsupportedOperationException("Mapbox integration not implemented");
    }
}
