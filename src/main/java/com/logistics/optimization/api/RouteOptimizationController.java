package com.logistics.optimization.api;
import com.logistics.optimization.application.RouteOptimizationWorkflow;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/routes")
public class RouteOptimizationController {
 private final RouteOptimizationWorkflow workflow;
 public RouteOptimizationController(RouteOptimizationWorkflow workflow){this.workflow=workflow;}
 @PostMapping("/optimize") @ResponseStatus(HttpStatus.CREATED)
 public RouteOptimizationWorkflow.OptimizationResponse optimize(@Valid @RequestBody OptimizeRouteRequest request,@RequestHeader(value="Idempotency-Key",required=false) String key){return workflow.optimize(request,key);}
}