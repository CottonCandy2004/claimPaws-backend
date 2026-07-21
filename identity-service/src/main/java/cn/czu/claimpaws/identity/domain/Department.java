package cn.czu.claimpaws.identity.domain;

import java.time.LocalDateTime;

public record Department(
        Long id,
        String name,
        Long parentId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean deleted
) {
}
