package com.logistics.optimization.adapters;
import com.logistics.optimization.domain.*;
import com.logistics.optimization.ports.RoutingEnginePort;
import org.springframework.stereotype.Component;
/** Synthetic offline adapter; this is not live Mapbox routing. */
@Component public class DeterministicRoutingAdapter implements RoutingEnginePort {
 public RouteOptimizationResult calculate(RouteOptimizationRequest request){if(request.stopAddresses().isEmpty())throw new IllegalArgumentException("At least one stop is required");return new RouteOptimizationResult(request.stopAddresses(),Math.max(1.0,request.stopAddresses().size()*4.2));}
}