package com.logistics.config;
import com.logistics.optimization.application.OptimizeRouteUseCase;
import com.logistics.optimization.application.RouteOptimizationService;
import com.logistics.optimization.ports.RoutingEnginePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Composition root for framework-neutral optimization application service. */
@Configuration
public class ApplicationConfiguration {
 @Bean
 public OptimizeRouteUseCase optimizeRouteUseCase(RoutingEnginePort routingEnginePort) {
  return new RouteOptimizationService(routingEnginePort);
 }
}
