package com.logistics.optimization.ports;

import com.logistics.optimization.domain.RouteOptimizationRequest;
import com.logistics.optimization.domain.RouteOptimizationResult;

/** Outbound port; keeps provider-specific APIs outside the Optimization Engine. */
public interface RoutingEnginePort {
    RouteOptimizationResult calculate(RouteOptimizationRequest request);
}
