package com.logistics.optimization.adapters;
import com.logistics.optimization.domain.*;
import com.logistics.optimization.ports.RoutingEnginePort;
/** Reserved for future live provider integration; deliberately not a Spring bean. */
public class MapboxAdapter implements RoutingEnginePort { public RouteOptimizationResult calculate(RouteOptimizationRequest request){throw new UnsupportedOperationException("Live Mapbox integration is not configured");} }