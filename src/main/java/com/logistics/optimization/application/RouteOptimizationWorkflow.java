package com.logistics.optimization.application;

import com.logistics.optimization.api.OptimizeRouteRequest;
import com.logistics.optimization.domain.RouteOptimizationRequest;
import com.logistics.optimization.domain.RouteOptimizationResult;
import com.logistics.optimization.ports.RoutingEnginePort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class RouteOptimizationWorkflow {
    private final JdbcTemplate jdbc;
    private final RoutingEnginePort routing;

    public RouteOptimizationWorkflow(JdbcTemplate jdbc, RoutingEnginePort routing) {
        this.jdbc = jdbc;
        this.routing = routing;
    }

    @Transactional
    public OptimizationResponse optimize(OptimizeRouteRequest request, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key header is required");
        }
        List<String> existing = jdbc.query(
                "SELECT route_id FROM idempotency_record WHERE idempotency_key = ?",
                (rs, row) -> rs.getString(1), idempotencyKey);
        if (!existing.isEmpty()) {
            return readResponse(existing.get(0));
        }

        Batch batch = jdbc.query(
                "SELECT status, all_orders_confirmed FROM order_batch WHERE batch_id = ? FOR UPDATE",
                rs -> rs.next() ? new Batch(rs.getString("status"), rs.getBoolean("all_orders_confirmed")) : null,
                request.batchId());
        if (batch == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Batch not found");
        if (!"Draft".equals(batch.status()) || !batch.confirmed()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Batch must be Draft and all orders confirmed");
        }
        if (request.totalWeightKg() > request.vehicleMaxWeightKg()
                || request.totalVolumeM3() > request.vehicleMaxVolumeM3()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Vehicle capacity exceeded");
        }

        RouteOptimizationRequest routingRequest = new RouteOptimizationRequest(
                request.stops().stream().map(OptimizeRouteRequest.StopRequest::address).toList());
        RouteOptimizationResult result;
        try {
            result = routing.calculate(routingRequest);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Routing dependency failed", ex);
        }

        if (request.stops().stream().anyMatch(stop -> !stop.reachable())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Route contains an unreachable stop");
        }
        int totalMinutes = (int) Math.ceil(result.totalDistanceKm() * 2) +
                request.stops().stream().mapToInt(OptimizeRouteRequest.StopRequest::serviceMinutes).sum();
        if (totalMinutes > request.driverShiftLimitMinutes()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Route exceeds driver shift limit");
        }
        int elapsed = 0;
        for (OptimizeRouteRequest.StopRequest stop : request.stops()) {
            elapsed += (int) Math.ceil(result.totalDistanceKm() * 2 / request.stops().size())
                    + stop.serviceMinutes();
            if (elapsed < stop.windowStartMinute() || elapsed > stop.windowEndMinute()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Estimated arrival falls outside a delivery window");
            }
        }

        String routeId = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO delivery_route(route_id,batch_id,vehicle_id,driver_id,status,total_distance_km,total_duration_minutes) VALUES (?,?,?,?,?,?,?)",
                routeId, request.batchId(), request.vehicleId(), request.driverId(), "Optimized",
                result.totalDistanceKm(), totalMinutes);
        int sequence = 1;
        for (String address : result.orderedStops()) {
            jdbc.update("INSERT INTO delivery_stop(route_id,sequence_no,address) VALUES (?,?,?)",
                    routeId, sequence++, address);
        }
        jdbc.update("UPDATE order_batch SET status='Optimized' WHERE batch_id=? AND status='Draft'", request.batchId());
        jdbc.update("INSERT INTO idempotency_record(idempotency_key,route_id) VALUES (?,?)", idempotencyKey, routeId);
        return readResponse(routeId);
    }

    private OptimizationResponse readResponse(String routeId) {
        RouteRow route = jdbc.queryForObject(
                "SELECT route_id,batch_id,status,total_distance_km,total_duration_minutes FROM delivery_route WHERE route_id=?",
                (rs, n) -> new RouteRow(rs.getString(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getInt(5)),
                routeId);
        List<String> stops = jdbc.query(
                "SELECT address FROM delivery_stop WHERE route_id=? ORDER BY sequence_no",
                (rs, n) -> rs.getString(1), routeId);
        return new OptimizationResponse(route.routeId(), route.batchId(), route.status(),
                route.distanceKm(), route.durationMinutes(), stops);
    }

    private record Batch(String status, boolean confirmed) {}
    private record RouteRow(String routeId, String batchId, String status, double distanceKm, int durationMinutes) {}
    public record OptimizationResponse(String routeId, String batchId, String status,
                                        double totalDistanceKm, int totalDurationMinutes, List<String> orderedStops) {}
}
