package cn.czu.claimpaws.reservation.infrastructure;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "resource-service", path = "/internal/v1/resources", configuration = FeignConfig.class, fallbackFactory = ResourceClientFallbackFactory.class)
public interface ResourceClient {
    @GetMapping("/{id}/reservation-snapshot")
    ReservationSnapshotDTO getSnapshot(@PathVariable("id") long resourceId);
}
