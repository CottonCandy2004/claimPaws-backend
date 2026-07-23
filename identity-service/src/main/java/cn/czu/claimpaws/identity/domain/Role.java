package cn.czu.claimpaws.identity.domain;

import java.time.LocalDateTime;

public record Role(
        Long id,
        String name,
        String code,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean deleted
) {
}
