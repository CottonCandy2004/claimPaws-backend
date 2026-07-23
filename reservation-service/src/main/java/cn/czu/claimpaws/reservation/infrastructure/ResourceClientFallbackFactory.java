package cn.czu.claimpaws.reservation.infrastructure;

import cn.czu.claimpaws.common.exception.BusinessException;
import cn.czu.claimpaws.common.exception.ErrorCode;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class ResourceClientFallbackFactory implements FallbackFactory<ResourceClient> {

    @Override
    public ResourceClient create(Throwable cause) {
        return resourceId -> new ReservationSnapshotDTO(
                new ReservationSnapshotDTO.ResourceInfo(resourceId, "default", "ROOM", 0, true),
                new ReservationSnapshotDTO.PolicyInfo(30, 7, 30, 240, false, 0),
                0L);
    }
}
