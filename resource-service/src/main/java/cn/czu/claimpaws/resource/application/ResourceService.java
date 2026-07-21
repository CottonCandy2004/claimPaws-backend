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
        ReservationPolicy policy = policyMapper.requireActiveByResourceId(resourceId);
        if (policy == null) {
            throw new IllegalArgumentException("No active policy for resource: " + resourceId);
        }
        return ReservationSnapshot.from(resource, policy);
    }
}
