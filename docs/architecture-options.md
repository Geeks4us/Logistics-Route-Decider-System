Architecture Options Analysis
1. Capability-Oriented Modular Monolith (Selected Alternative)
 Description: The application is structured as a single deployment unit, but the codebase is strictly partitioned by domain capabilities (such as the Dispatch & Management, Optimization Engine, and Tracking & Notification Subsystems) rather than horizontal technical layers. Communication across domain boundaries is handled via in-memory procedure calls and local events. 

2. Traditional Layered Monolith
 Description: The system is organized strictly by horizontal technical layers: a Presentation Layer (UI), a Business Logic Layer, and a Data Access Layer, where each layer depends entirely on the layer immediately beneath it.

3. Event-Driven Microservices Architecture
 Description: The system is split into completely independent, separately deployable services (such as a Dispatch Service, Optimization Engine Service, and Notification Service) that communicate asynchronously over a network message broker (e.g., RabbitMQ or Kafka) with decentralized data stores.
