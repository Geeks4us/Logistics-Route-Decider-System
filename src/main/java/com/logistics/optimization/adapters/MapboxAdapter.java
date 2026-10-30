package com.logistics.optimization.adapters;

import com.logistics.optimization.domain.RouteOptimizationRequest;
import com.logistics.optimization.domain.RouteOptimizationResult;
import com.logistics.optimization.ports.RoutingEnginePort;

/** Reserved for a future real provider integration; not registered as an active Spring bean. */
public class MapboxAdapter implements RoutingEnginePort {
    @Override
    public RouteOptimizationResult calculate(RouteOptimizationRequest request) {
        throw new UnsupportedOperationException("Live Mapbox integration is not configured");
    }
}
