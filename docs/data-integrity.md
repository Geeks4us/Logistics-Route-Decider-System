# Data Integrity and Main Risk Register

## Main integrity risk

The highest-risk failure is a **partially or concurrently applied route assignment**: two dispatchers optimize/assign overlapping orders or the same driver/vehicle, or one part of a multi-entity update succeeds while another fails. This could leave an order on multiple active routes, a driver marked Available while assigned, or a batch/route lifecycle mismatch.

## Integrity controls

| Risk / invariant | Enforcement |
|---|---|
| Duplicate order membership in a batch | Composite primary key on BatchOrder(batch_id, order_id); validate that an order is not in another active batch where required. |
| Order on two active routes | Transactionally recheck active assignments on the primary; add a filtered/partial unique constraint on active order assignment if supported. Keep historical route stops for audit. |
| Driver assigned to multiple active routes | Primary-database transaction, optimistic version check on Driver, and filtered/partial unique active assignment constraint where supported. |
| Vehicle assigned to multiple active routes | Same approach as driver: version check plus unique active assignment constraint. |
| Route/batch state mismatch | Update DeliveryRoute and OrderBatch in one primary-database transaction; enforce permitted state transitions in the application service. |
| Invalid references or orphaned stops | Foreign keys from DeliveryRoute to OrderBatch/Driver/Vehicle and from DeliveryStop to DeliveryRoute/DeliveryOrder. |
| Duplicate stop order / repeated order in route | UNIQUE(route_id, sequence_no), UNIQUE(route_id, order_id), and positive sequence check. |
| Capacity, shift or delivery-window violation | Validate aggregate payload weight/volume, estimated shift duration and ETA windows before approval; recheck relevant versions at commit. |
| Lost update from concurrent dispatchers | Optimistic locking/version columns; reject stale writes with a conflict response and require refresh. |
| Retry after client timeout creates duplicate route | Idempotency-Key persisted on the primary with a unique scope constraint and stored response/result. |
| Stale replica read influences a write decision | All writes and consistency-sensitive validation reads use the primary. Replica is read-only and only for explicitly stale-tolerant views. |
| Primary/replica lag or replica outage | Monitor replication delay/health; route eligible reads to primary or return a controlled read error. Do not acknowledge a write based only on replica state. |

## Transaction boundaries

### Optimize route

The application validates the batch and resources, obtains external routing data, calculates the proposal, then commits the following together on the primary:

- DeliveryRoute row;
- ordered DeliveryStop rows;
- OrderBatch Draft -> Optimized transition;
- idempotency record and response reference.

If provider lookup or domain validation fails before commit, no route record is created. If the transaction fails, all database changes roll back.

### Dispatch / assign route

The dispatcher approval and assignment operation must recheck route, driver, vehicle and order versions/availability on the primary. In one transaction it changes route state to Dispatched, changes driver/vehicle availability, and records the active assignment. Notification delivery occurs after commit; notification failure must not roll back a valid dispatch. The notification can be retried using an outbox or durable retry record if implemented.

## Primary and replica rules

- Primary is the sole write authority.
- Replica receives primary changes and is never written by application code.
- Dashboard/history reads may use the replica only if the UI can tolerate lag and displays appropriate freshness semantics.
- Route optimization, assignment, duplicate checks, availability checks, lifecycle transitions and read-after-write requests must use the primary.
- Replication is not a backup. Backups and restore testing remain separate operational controls.
- No automatic failover is claimed until a failover policy and recovery test are documented.

## Verification plan

1. Integration test concurrent attempts to assign the same driver, vehicle and order; exactly one transaction must succeed.
2. Force a failure between route and stop persistence; verify the transaction leaves no partial route and batch remains Draft.
3. Retry an identical request with the same idempotency key; verify the same route/result is returned without a duplicate.
4. Reuse an idempotency key with a different request; verify HTTP 409.
5. Simulate replica lag; verify write validation and immediate post-write reads still use the primary.
6. Simulate replica unavailability; verify eligible reads fall back or fail safely and writes remain directed to primary.
7. Simulate primary failure; verify behavior matches only the configured recovery/failover procedure.

**Evidence status:** These are required controls and planned tests. Do not report them as passed until test output and deployment evidence are recorded.
