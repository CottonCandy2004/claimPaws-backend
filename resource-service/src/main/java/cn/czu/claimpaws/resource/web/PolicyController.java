package cn.czu.claimpaws.resource.web;

import cn.czu.claimpaws.common.api.ApiResponse;
import cn.czu.claimpaws.resource.domain.ReservationPolicy;
import cn.czu.claimpaws.resource.persistence.PolicyMapper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {

    private final PolicyMapper policyMapper;

    public PolicyController(PolicyMapper policyMapper) {
        this.policyMapper = policyMapper;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword,
            @RequestHeader("X-Request-Id") String requestId) {
        int offset = (page - 1) * size;
        List<ReservationPolicy> records = policyMapper.findPage(offset, size, keyword);
        long total = policyMapper.count(keyword);

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        return ApiResponse.success(data, requestId);
    }

    @GetMapping("/{id}")
    public ApiResponse<ReservationPolicy> getById(
            @PathVariable long id,
            @RequestHeader("X-Request-Id") String requestId) {
        ReservationPolicy policy = policyMapper.findById(id);
        if (policy == null) {
            return ApiResponse.failure("NOT_FOUND", "Policy not found", requestId);
        }
        return ApiResponse.success(policy, requestId);
    }

    @GetMapping("/by-resource/{resourceId}")
    public ApiResponse<ReservationPolicy> getByResourceId(
            @PathVariable long resourceId,
            @RequestHeader("X-Request-Id") String requestId) {
        ReservationPolicy policy = policyMapper.findByResourceId(resourceId);
        if (policy == null) {
            return ApiResponse.failure("NOT_FOUND", "Policy not found for resource", requestId);
        }
        return ApiResponse.success(policy, requestId);
    }

    @PostMapping
    public ApiResponse<ReservationPolicy> create(
            @RequestBody Map<String, Object> body,
            @RequestHeader("X-Request-Id") String requestId) {
        ReservationPolicy policy = new ReservationPolicy(
                null,
                body.get("resourceId") != null ? Long.valueOf(body.get("resourceId").toString()) : 0L,
                body.get("timeSlotGranularity") != null ? Integer.valueOf(body.get("timeSlotGranularity").toString()) : 30,
                body.get("advanceBookingDays") != null ? Integer.valueOf(body.get("advanceBookingDays").toString()) : 7,
                body.get("minDuration") != null ? Integer.valueOf(body.get("minDuration").toString()) : 30,
                body.get("maxDuration") != null ? Integer.valueOf(body.get("maxDuration").toString()) : 240,
                body.get("cancelDeadline") != null ? Integer.valueOf(body.get("cancelDeadline").toString()) : 60,
                body.get("checkInWindow") != null ? Integer.valueOf(body.get("checkInWindow").toString()) : 15,
                body.get("approvalLevel") != null && Integer.valueOf(body.get("approvalLevel").toString()) > 0,
                body.get("approvalLevel") != null ? Integer.valueOf(body.get("approvalLevel").toString()) : 0,
                true, null, null, null
        );
        policyMapper.insert(policy);
        return ApiResponse.success(policy, requestId);
    }

    @PutMapping("/{id}")
    public ApiResponse<ReservationPolicy> update(
            @PathVariable long id,
            @RequestBody ReservationPolicy policy,
            @RequestHeader("X-Request-Id") String requestId) {
        ReservationPolicy existing = policyMapper.findById(id);
        if (existing == null) {
            return ApiResponse.failure("NOT_FOUND", "Policy not found", requestId);
        }
        ReservationPolicy toUpdate = new ReservationPolicy(
                id,
                policy.resourceId(),
                policy.slotMinutes(),
                policy.advanceDays(),
                policy.minDurationMinutes(),
                policy.maxDurationMinutes(),
                policy.cancelDeadlineMinutes(),
                policy.checkInWindowMinutes(),
                policy.requiresApproval(),
                policy.approvalLevel(),
                policy.active(),
                null,
                null,
                existing.deleted()
        );
        policyMapper.update(toUpdate);
        ReservationPolicy updated = policyMapper.findById(id);
        return ApiResponse.success(updated, requestId);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable long id,
            @RequestHeader("X-Request-Id") String requestId) {
        ReservationPolicy existing = policyMapper.findById(id);
        if (existing == null) {
            return ApiResponse.failure("NOT_FOUND", "Policy not found", requestId);
        }
        policyMapper.deleteById(id);
        return ApiResponse.success(null, requestId);
    }
}
