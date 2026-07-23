package cn.czu.claimpaws.resource.web;

import cn.czu.claimpaws.common.api.ApiResponse;
import cn.czu.claimpaws.resource.application.ResourceService;
import cn.czu.claimpaws.resource.domain.ReservationSnapshot;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/{id}/reservation-snapshot")
    public ApiResponse<ReservationSnapshot> getReservationSnapshot(
            @PathVariable long id,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        ReservationSnapshot snapshot = resourceService.snapshot(id);
        return ApiResponse.success(snapshot, requestId);
    }
}
