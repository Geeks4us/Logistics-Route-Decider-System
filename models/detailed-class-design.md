# Detailed Class and Interface Design

**System:** Logistics Route Decider System  
**Group:** CSI473 Group 01 (Geeks4us)  
**Document Ref:** LAB09-CLASS-DESIGN-001  

---

## 1. Overview & Core Workflow Scope

This document details the class structure, interface boundaries, and dependency directions for the core workflow of the **Logistics Route Decider System**. The design models the process from receiving an incoming dispatch route request, validating idempotency, querying geospatial routing engines through ports, applying dynamic scoring strategies based on vehicle constraints, and persisting the route decision.

---

## 2. Detailed Class Diagram

```mermaid
classDiagram
    class RouteOptimizationService {
        - RoutingEnginePort routingEngine
        - RouteRepository routeRepository
        - List~RouteScoringStrategy~ scoringStrategies
        - IdempotencyLedgerManager ledgerManager
        + calculateOptimalRoute(RouteRequest request) RouteResponse
    }

    class RoutingEnginePort {
        <<interface>>
        + fetchExternalRoutes(Waypoint origin, Waypoint destination) RawRouteData
    }

    class MapboxAdapter {
        - HttpClient httpClient
        - CircuitBreaker circuitBreaker
        + fetchExternalRoutes(Waypoint origin, Waypoint destination) RawRouteData
    }

    class RouteScoringStrategy {
        <<interface>>
        + double evaluate(RouteSegment segment, VehicleConstraints constraints)
    }

    class DistanceCostStrategy {
        + double evaluate(RouteSegment segment, VehicleConstraints constraints)
    }

    class TollAndWeightStrategy {
        + double evaluate(RouteSegment segment, VehicleConstraints constraints)
    }

    class RouteRepository {
        <<interface>>
        + save(CalculatedRoute route) UUID
        + findById(UUID id) Optional~CalculatedRoute~
    }

    class IdempotencyLedgerManager {
        - RedisTemplate redisTemplate
        + boolean acquireLock(String requestId)
        + void updateStatus(String requestId, String status, String responsePayload)
    }

    RouteOptimizationService --> RoutingEnginePort : depends on abstraction
    RoutingEnginePort <|.. MapboxAdapter : implements (outbound adapter)
    RouteOptimizationService --> RouteScoringStrategy : delegates strategy
    RouteScoringStrategy <|.. DistanceCostStrategy : implements
    RouteScoringStrategy <|.. TollAndWeightStrategy : implements
    RouteOptimizationService --> RouteRepository : persists via
    RouteOptimizationService --> IdempotencyLedgerManager : guards duplicates via
