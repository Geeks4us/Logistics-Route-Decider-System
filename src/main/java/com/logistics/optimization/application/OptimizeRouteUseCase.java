package com.logistics.optimization.application;

import com.logistics.optimization.domain.RouteOptimizationRequest;
import com.logistics.optimization.domain.RouteOptimizationResult;

/** Public application contract consumed by other capability modules. */
public interface OptimizeRouteUseCase {
    RouteOptimizationResult optimize(RouteOptimizationRequest request);
}
