package com.logistics.optimization.adapters;

import com.logistics.optimization.domain.RouteOptimizationRequest;
import com.logistics.optimization.domain.RouteOptimizationResult;
import com.logistics.optimization.ports.RoutingEnginePort;
import org.springframework.stereotype.Component;

/**
 * Offline demonstration adapter. This is deliberately synthetic, not live Mapbox routing.
 * Replace with a provider adapter when credentials and provider integration are available.
 */
@Component
public class DeterministicRoutingAdapter implements RoutingEnginePort {
    @Override
    public RouteOptimizationResult calculate(RouteOptimizationRequest request) {
        if (request.stopAddresses().isEmpty()) {
            throw new IllegalArgumentException("At least one stop is required");
        }
        return new RouteOptimizationResult(request.stopAddresses(),
                Math.max(1.0, request.stopAddresses().size() * 4.2));
    }
}
