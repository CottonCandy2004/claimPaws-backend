package cn.czu.claimpaws.reservation.infrastructure;

import cn.czu.claimpaws.common.exception.BusinessException;
import cn.czu.claimpaws.common.exception.ErrorCode;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class ResourceClientFallbackFactory implements FallbackFactory<ResourceClient> {

    @Override
    public ResourceClient create(Throwable cause) {
        return resourceId -> {
            throw new BusinessException(ErrorCode.RESOURCE_SERVICE_UNAVAILABLE);
        };
    }
}
