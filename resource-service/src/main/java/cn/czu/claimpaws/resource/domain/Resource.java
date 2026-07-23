package cn.czu.claimpaws.resource.domain;

import java.time.LocalDateTime;

public record Resource(
        Long id,
        String name,
        String type,
        String floor,
        String building,
        Integer capacity,
        String description,
        Long policyId,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean deleted
) {
}
