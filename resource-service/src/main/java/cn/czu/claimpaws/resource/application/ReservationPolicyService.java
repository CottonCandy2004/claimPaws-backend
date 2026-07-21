package cn.czu.claimpaws.resource.application;

import cn.czu.claimpaws.resource.domain.ReservationPolicy;
import cn.czu.claimpaws.resource.persistence.PolicyMapper;
import org.springframework.stereotype.Service;

@Service
public class ReservationPolicyService {

    private final PolicyMapper policyMapper;

    public ReservationPolicyService(PolicyMapper policyMapper) {
        this.policyMapper = policyMapper;
    }

    public ReservationPolicy getActivePolicyByResourceId(long resourceId) {
        return policyMapper.requireActiveByResourceId(resourceId);
    }
}
