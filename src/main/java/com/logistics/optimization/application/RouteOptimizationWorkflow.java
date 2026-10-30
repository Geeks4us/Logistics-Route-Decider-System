package com.logistics.optimization.application;
import com.logistics.optimization.api.OptimizeRouteRequest;
import com.logistics.optimization.domain.RouteOptimizationRequest;
import com.logistics.optimization.domain.RouteOptimizationResult;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;

@Service
public class RouteOptimizationWorkflow {
 private final JdbcTemplate jdbc; private final OptimizeRouteUseCase optimizer;
 public RouteOptimizationWorkflow(JdbcTemplate jdbc,OptimizeRouteUseCase optimizer){this.jdbc=jdbc;this.optimizer=optimizer;}
 @Transactional
 public OptimizationResponse optimize(OptimizeRouteRequest request,String key){
  if(key==null||key.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Idempotency-Key header is required");
  List<String> prior=jdbc.query("SELECT route_id FROM idempotency_record WHERE idempotency_key=?",(rs,n)->rs.getString(1),key);
  if(!prior.isEmpty())return readResponse(prior.get(0));
  Batch batch=jdbc.query("SELECT status,all_orders_confirmed FROM order_batch WHERE batch_id=? FOR UPDATE",rs->rs.next()?new Batch(rs.getString(1),rs.getBoolean(2)):null,request.batchId());
  if(batch==null)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Batch not found");
  if(!"Draft".equals(batch.status())||!batch.confirmed())throw new ResponseStatusException(HttpStatus.CONFLICT,"Batch must be Draft and all orders confirmed");
  if(request.totalWeightKg()>request.vehicleMaxWeightKg()||request.totalVolumeM3()>request.vehicleMaxVolumeM3())throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Vehicle capacity exceeded");
  RouteOptimizationResult result;
  try{result=optimizer.optimize(new RouteOptimizationRequest(request.stops().stream().map(OptimizeRouteRequest.StopRequest::address).toList()));}
  catch(RuntimeException ex){throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Routing dependency failed",ex);}
  if(request.stops().stream().anyMatch(s->!s.reachable()))throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Route contains an unreachable stop");
  int driveMinutes=(int)Math.ceil(result.totalDistanceKm()*2);
  int totalMinutes=driveMinutes+request.stops().stream().mapToInt(OptimizeRouteRequest.StopRequest::serviceMinutes).sum();
  if(totalMinutes>request.driverShiftLimitMinutes())throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Route exceeds driver shift limit");
  int elapsed=0; int perLeg=(int)Math.ceil((double)driveMinutes/request.stops().size());
  for(var stop:request.stops()){elapsed+=perLeg+stop.serviceMinutes();if(elapsed<stop.windowStartMinute()||elapsed>stop.windowEndMinute())throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Estimated arrival falls outside a delivery window");}
  String routeId=UUID.randomUUID().toString();
  jdbc.update("INSERT INTO delivery_route(route_id,batch_id,vehicle_id,driver_id,status,total_distance_km,total_duration_minutes) VALUES (?,?,?,?,?,?,?)",routeId,request.batchId(),request.vehicleId(),request.driverId(),"Optimized",result.totalDistanceKm(),totalMinutes);
  int sequence=1;for(String address:result.orderedStops())jdbc.update("INSERT INTO delivery_stop(route_id,sequence_no,address) VALUES (?,?,?)",routeId,sequence++,address);
  jdbc.update("UPDATE order_batch SET status='Optimized' WHERE batch_id=? AND status='Draft'",request.batchId());
  jdbc.update("INSERT INTO idempotency_record(idempotency_key,route_id) VALUES (?,?)",key,routeId);
  return readResponse(routeId);
 }
 private OptimizationResponse readResponse(String id){
  RouteRow row=jdbc.queryForObject("SELECT route_id,batch_id,status,total_distance_km,total_duration_minutes FROM delivery_route WHERE route_id=?",(rs,n)->new RouteRow(rs.getString(1),rs.getString(2),rs.getString(3),rs.getDouble(4),rs.getInt(5)),id);
  List<String> stops=jdbc.query("SELECT address FROM delivery_stop WHERE route_id=? ORDER BY sequence_no",(rs,n)->rs.getString(1),id);
  return new OptimizationResponse(row.id(),row.batch(),row.status(),row.distance(),row.duration(),stops);
 }
 private record Batch(String status,boolean confirmed){}
 private record RouteRow(String id,String batch,String status,double distance,int duration){}
 public record OptimizationResponse(String routeId,String batchId,String status,double totalDistanceKm,int totalDurationMinutes,List<String> orderedStops){}
}