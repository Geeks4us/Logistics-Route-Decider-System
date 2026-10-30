package com.logistics.dispatch.ports;

import com.logistics.dispatch.domain.Route;
import java.util.Optional;
import java.util.UUID;

/** Outbound persistence port owned by Dispatch & Management. */
public interface RouteRepository {
    Optional<Route> findById(UUID routeId);
    Route save(Route route);
}
