# Lab 05 Consistency Matrix

| Requirement ID | Use Case Step | Sequence Message | Domain Entity & Class | Lifecycle State |
| :--- | :--- | :--- | :--- | :--- |
| **FR02** / **FR03** | UC-02 Step 1–2 | `validatePayload()` | `Vehicle`: Checks max weight/volume limits | `Draft` |
| **FR09** | UC-02 Step 3 | `fetchDistanceMatrix()` | `ExternalMappingService`: Provides road data | `Draft` -> `Optimized` |
| **FR04** / **FR05** | UC-02 Step 4–5 | `createOptimizedRoute()` | `DeliveryRoute`: Transitions state to assigned | `Optimized` -> `Assigned` |
