package com.logistics.optimization.domain;

import java.util.List;

/** Framework- and provider-independent optimization result. */
public record RouteOptimizationResult(List<String> orderedStops, double totalDistanceKm) {
    public RouteOptimizationResult {
        orderedStops = List.copyOf(orderedStops);
    }
}
