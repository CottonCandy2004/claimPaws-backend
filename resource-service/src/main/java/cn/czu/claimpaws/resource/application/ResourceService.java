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
        try {
            Resource resource = resourceMapper.requireActive(resourceId);
            if (resource == null) {
                return defaultSnapshot(resourceId);
            }
            ReservationPolicy policy = resource.policyId() != null
                    ? policyMapper.findById(resource.policyId())
                    : policyMapper.requireActiveByResourceId(resourceId);
            if (policy == null) {
                policy = new ReservationPolicy(0L, 0L, "default", null, 1, 7, 30, 240, 60, 15, false, 0, "", true, null, null, false);
            }
            return ReservationSnapshot.from(resource, policy);
        } catch (Exception e) {
            return defaultSnapshot(resourceId);
        }
    }

    private ReservationSnapshot defaultSnapshot(long resourceId) {
        Resource resource = new Resource(resourceId, "default", "ROOM", "", "", 0, "", null, true, null, null, false);
        ReservationPolicy policy = new ReservationPolicy(0L, 0L, "default", null, 1, 7, 30, 240, 60, 15, false, 0, "", true, null, null, false);
        return ReservationSnapshot.from(resource, policy);
    }
}
