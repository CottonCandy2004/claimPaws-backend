package cn.czu.claimpaws.resource.application;

import cn.czu.claimpaws.resource.domain.ReservationSnapshot;
import cn.czu.claimpaws.resource.domain.Resource;
import cn.czu.claimpaws.resource.domain.ReservationPolicy;
import cn.czu.claimpaws.resource.persistence.PolicyMapper;
import cn.czu.claimpaws.resource.persistence.ResourceMapper;
import org.springframework.stereotype.Service;

@Service
public class ResourceService {

    private final ResourceMapper resourceMapper;
    private final PolicyMapper policyMapper;

    public ResourceService(ResourceMapper resourceMapper, PolicyMapper policyMapper) {
        this.resourceMapper = resourceMapper;
        this.policyMapper = policyMapper;
    }

    public ReservationSnapshot snapshot(long resourceId) {
        // TODO: migrate to BusinessException with stable error codes
        Resource resource = resourceMapper.requireActive(resourceId);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found or inactive: " + resourceId);
        }
        ReservationPolicy policy = resource.policyId() != null
                ? policyMapper.findById(resource.policyId())
                : policyMapper.requireActiveByResourceId(resourceId);
        if (policy == null) {
            // Return default policy
            policy = new ReservationPolicy(0L, 0L, "default", null, 30, 7, 30, 240, 60, 15, false, 0, "", true, null, null, false);
        }
        return ReservationSnapshot.from(resource, policy);
    }
}
