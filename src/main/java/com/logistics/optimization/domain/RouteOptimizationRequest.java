package com.logistics.optimization.domain;

import java.util.List;

/** Framework- and provider-independent input model. */
public record RouteOptimizationRequest(List<String> stopAddresses) {
    public RouteOptimizationRequest {
        stopAddresses = List.copyOf(stopAddresses);
    }
}
