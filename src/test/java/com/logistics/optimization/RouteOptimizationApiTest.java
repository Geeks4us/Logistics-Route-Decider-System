package com.logistics.optimization;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.optimization.api.OptimizeRouteRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class RouteOptimizationApiTest {
 @Autowired MockMvc mvc; @Autowired ObjectMapper mapper; @Autowired JdbcTemplate jdbc;
 @BeforeEach void resetDatabase(){
  jdbc.update("DELETE FROM idempotency_record");jdbc.update("DELETE FROM delivery_stop");jdbc.update("DELETE FROM delivery_route");
  jdbc.update("MERGE INTO order_batch(batch_id,status,all_orders_confirmed) KEY(batch_id) VALUES('BATCH-DEMO-001','Draft',TRUE)");
 }
 private OptimizeRouteRequest valid(String batch){
  return new OptimizeRouteRequest(batch,"VAN-01","DRIVER-01",1000,10,240,250,2,
   List.of(new OptimizeRouteRequest.StopRequest("Gaborone CBD",10,true,0,180)));
 }
 @Test void persistsRouteAndTransitionsBatchOnSuccess() throws Exception {
  mvc.perform(post("/api/routes/optimize").header("Idempotency-Key","success-001").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(valid("BATCH-DEMO-001"))))
   .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("Optimized")).andExpect(jsonPath("$.orderedStops[0]").value("Gaborone CBD"));
  org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject("SELECT status FROM order_batch WHERE batch_id='BATCH-DEMO-001'",String.class)).isEqualTo("Optimized");
  org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM delivery_route",Integer.class)).isEqualTo(1);
 }
 @Test void rejectsCapacityAndDoesNotPersistRoute() throws Exception {
  OptimizeRouteRequest invalid=new OptimizeRouteRequest("BATCH-DEMO-001","VAN-01","DRIVER-01",100,1,240,250,2,List.of(new OptimizeRouteRequest.StopRequest("Gaborone CBD",10,true,0,180)));
  mvc.perform(post("/api/routes/optimize").header("Idempotency-Key","capacity-001").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(invalid))).andExpect(status().isUnprocessableEntity());
  org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM delivery_route",Integer.class)).isZero();
  org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject("SELECT status FROM order_batch WHERE batch_id='BATCH-DEMO-001'",String.class)).isEqualTo("Draft");
 }
 @Test void rejectsUnreachableStopWithoutCreatingRoute() throws Exception {
  OptimizeRouteRequest invalid=new OptimizeRouteRequest("BATCH-DEMO-001","VAN-01","DRIVER-01",1000,10,240,250,2,List.of(new OptimizeRouteRequest.StopRequest("Remote stop",10,false,0,180)));
  mvc.perform(post("/api/routes/optimize").header("Idempotency-Key","unreachable-001").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(invalid))).andExpect(status().isUnprocessableEntity());
  org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM delivery_route",Integer.class)).isZero();
 }
}
