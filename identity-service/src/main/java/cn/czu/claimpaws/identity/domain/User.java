package cn.czu.claimpaws.identity.domain;

import java.time.LocalDateTime;

public record User(
        Long id,
        String username,
        String passwordHash,
        String displayName,
        String email,
        String phone,
        Long departmentId,
        Boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean deleted
) {
    public boolean isActive() {
        return Boolean.TRUE.equals(enabled) && !Boolean.TRUE.equals(deleted);
    }
}
