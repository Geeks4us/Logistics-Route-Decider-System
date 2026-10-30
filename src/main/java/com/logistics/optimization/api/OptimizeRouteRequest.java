package com.logistics.optimization.api;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
public record OptimizeRouteRequest(@NotBlank String batchId,@NotBlank String vehicleId,@NotBlank String driverId,@Positive double vehicleMaxWeightKg,@Positive double vehicleMaxVolumeM3,@Positive int driverShiftLimitMinutes,@PositiveOrZero double totalWeightKg,@PositiveOrZero double totalVolumeM3,@NotEmpty List<@Valid StopRequest> stops) {
 public record StopRequest(@NotBlank String address,@PositiveOrZero int serviceMinutes,boolean reachable,@PositiveOrZero int windowStartMinute,@Positive int windowEndMinute) {}
}