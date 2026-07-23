package cn.czu.claimpaws.resource.web;

import cn.czu.claimpaws.common.api.ApiResponse;
import cn.czu.claimpaws.resource.domain.ReservationPolicy;
import cn.czu.claimpaws.resource.persistence.PolicyMapper;
import cn.czu.claimpaws.resource.persistence.ResourceMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {

    private final PolicyMapper policyMapper;
    private final ResourceMapper resourceMapper;

    public PolicyController(PolicyMapper policyMapper, ResourceMapper resourceMapper) {
        this.policyMapper = policyMapper;
        this.resourceMapper = resourceMapper;
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
                (String) body.getOrDefault("name", ""),
                (String) body.getOrDefault("resourceType", "MEETING_ROOM"),
                body.get("timeSlotGranularity") != null ? Integer.valueOf(body.get("timeSlotGranularity").toString()) : 30,
                body.get("advanceBookingDays") != null ? Integer.valueOf(body.get("advanceBookingDays").toString()) : 7,
                body.get("minDuration") != null ? Integer.valueOf(body.get("minDuration").toString()) : 30,
                body.get("maxDuration") != null ? Integer.valueOf(body.get("maxDuration").toString()) : 240,
                body.get("cancelDeadline") != null ? Integer.valueOf(body.get("cancelDeadline").toString()) : 60,
                body.get("checkInWindow") != null ? Integer.valueOf(body.get("checkInWindow").toString()) : 15,
                body.get("approvalLevel") != null && Integer.valueOf(body.get("approvalLevel").toString()) > 0,
                body.get("approvalLevel") != null ? Integer.valueOf(body.get("approvalLevel").toString()) : 0,
                (String) body.getOrDefault("approverRoles", (String) body.getOrDefault("description", "")),
                true, null, null, null
        );
        policyMapper.insert(policy);
        updateResourcePolicyIds(body, policy.id());
        return ApiResponse.success(policy, requestId);
    }

    private void updateResourcePolicyIds(Map<String, Object> body, Long policyId) {
        Object ids = body.get("resourceIds");
        if (ids instanceof List<?> list && !list.isEmpty()) {
            for (Object id : list) {
                if (id instanceof Number) {
                    resourceMapper.updatePolicyId(((Number) id).longValue(), policyId);
                }
            }
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<ReservationPolicy> update(
            @PathVariable long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader("X-Request-Id") String requestId) {
        ReservationPolicy existing = policyMapper.findById(id);
        if (existing == null) {
            return ApiResponse.failure("NOT_FOUND", "Policy not found", requestId);
        }
        ReservationPolicy toUpdate = new ReservationPolicy(
                id,
                body.containsKey("resourceId") ? Long.valueOf(body.get("resourceId").toString()) : existing.resourceId(),
                body.containsKey("name") ? (String) body.get("name") : existing.name(),
                body.containsKey("resourceType") ? (String) body.get("resourceType") : existing.resourceType(),
                body.containsKey("timeSlotGranularity") ? Integer.valueOf(body.get("timeSlotGranularity").toString()) : existing.slotMinutes(),
                body.containsKey("advanceBookingDays") ? Integer.valueOf(body.get("advanceBookingDays").toString()) : existing.advanceDays(),
                body.containsKey("minDuration") ? Integer.valueOf(body.get("minDuration").toString()) : existing.minDurationMinutes(),
                body.containsKey("maxDuration") ? Integer.valueOf(body.get("maxDuration").toString()) : existing.maxDurationMinutes(),
                body.containsKey("cancelDeadline") ? Integer.valueOf(body.get("cancelDeadline").toString()) : existing.cancelDeadlineMinutes(),
                body.containsKey("checkInWindow") ? Integer.valueOf(body.get("checkInWindow").toString()) : existing.checkInWindowMinutes(),
                body.containsKey("approvalLevel") ? Integer.valueOf(body.get("approvalLevel").toString()) > 0 : existing.requiresApproval(),
                body.containsKey("approvalLevel") ? Integer.valueOf(body.get("approvalLevel").toString()) : existing.approvalLevel(),
                body.containsKey("approverRoles") ? (String) body.get("approverRoles") : existing.description(),
                existing.active(), null, null, existing.deleted()
        );
        policyMapper.update(toUpdate);
        updateResourcePolicyIds(body, id);
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
